package com.djeremy.process.monitor.adapter.streams.process

import com.djeremy.avro.business.process.monitor.ProcessStep
import com.djeremy.process.monitor.adapter.streams.CustomizedKafkaProperties
import com.djeremy.process.monitor.adapter.streams.StreamDefinition
import com.djeremy.process.monitor.domain.process.StepService
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.Reference
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.process.models.StepId
import mu.KotlinLogging.logger
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import java.time.LocalDateTime.ofInstant
import java.time.ZoneId.systemDefault
import java.util.*

class ProcessStepStreamDefinitionFactory(
    private val applicationId: String,
    private val stepEventTopic: String,
    private val kafkaProperties: CustomizedKafkaProperties,
    private val stepService: StepService
) {

    fun build() = StreamDefinition(
        applicationId, stream()
    )

    private fun stream(): KafkaStreams {
        val logger = logger(applicationId)
        val props = kafkaProperties.buildStreamsPropertiesForApplication(applicationId)
        val topology = with(StreamsBuilder()) {
            stream<String, SpecificRecord>(stepEventTopic)
                .foreach { _, value ->
                    val resolvedStep = value.resolveStepModel()
                    if (resolvedStep != null) {
                        stepService.processStep(resolvedStep).onFail {
                            logger.error {
                                "Failed to process step with id [${resolvedStep.stepId}] belonging to " +
                                        "configuration [${resolvedStep.configurationId}] with fail message: $reason"
                            }
                        }
                    } else {
                        logger.warn { "Received unsupported record type with schema [${value.schema}]" }
                    }
                }
            build()
        }
        return KafkaStreams(topology, props)
    }
}

fun SpecificRecord.resolveStepModel(): Step? =
    when (this) {
        is ProcessStep -> toModel()
        else -> null
    }

fun ProcessStep.toModel(): Step {
    val references = getReferences().map { Reference(it.getId(), it.getName()) }

    return Step(
        id = UUID.randomUUID().toString(),
        configurationId = ProcessConfigurationId(getConfigurationId()),
        stepId = StepId(getStepId()),
        eventId = getEventId(),
        receivedAt = ofInstant(getReceivedAt(), systemDefault()),
        references = references,
        isLast = getIsLast()
    )
}