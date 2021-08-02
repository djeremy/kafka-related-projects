package com.djeremy.process.monitor.adapter.streams

import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.step.StepStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.step.StepTransformationStreamBuilder
import com.djeremy.process.monitor.domain.transformation.TopicTransformations
import mu.KotlinLogging.logger
import org.apache.kafka.streams.KafkaStreams

interface StreamsRegistration {
    fun initialize(transformations: Set<TopicTransformations>)
    fun register(transformations: TopicTransformations)
    fun register(applicationId: String, kafkaStreams: KafkaStreams)
}

class DefaultStreamsRegistration(
    private val stepStreamsRegistry: StepStreamsRegistry,
    private val applicationStreamRegistry: ApplicationStreamsRegistry,
    private val stepTransformationStreamBuilder: StepTransformationStreamBuilder
) : StreamsRegistration {

    private val log = logger { }

    override fun initialize(transformations: Set<TopicTransformations>) {
        stepStreamsRegistry.stopAllAndClean().run { log.info { "Stopping all running streams" } }

        transformations.forEach { topicTransformations ->
            stepStreamsRegistry.add(topicTransformations,
                stepTransformationStreamBuilder.buildStreamFor(topicTransformations)
                    .also { it.logTopologies() })
        }

        stepStreamsRegistry.startAll().run { log.info { "Starting all streams" } }
    }

    override fun register(transformations: TopicTransformations) = with(transformations) {
        if (stepStreamsRegistry.isRegisteredByTopic(topic)) {
            stepStreamsRegistry.stopByTopic(topic)

            stepStreamsRegistry.add(this, stepTransformationStreamBuilder.buildStreamFor(this))
            stepStreamsRegistry.startByTopic(topic)
        } else {
            stepStreamsRegistry.add(this, stepTransformationStreamBuilder.buildStreamFor(this))
            stepStreamsRegistry.startByTopic(this.topic)
        }
    }

    override fun register(applicationId: String, kafkaStreams: KafkaStreams) {
        if (applicationStreamRegistry.isRegisteredByApplicationId(applicationId)) {
            applicationStreamRegistry.stopByApplicationId(applicationId)

            addAndStartByApplicationId(applicationId, kafkaStreams)
        } else {
            addAndStartByApplicationId(applicationId, kafkaStreams)
        }
    }

    private fun addAndStartByApplicationId(applicationId: String, kafkaStreams: KafkaStreams) {
        applicationStreamRegistry.add(applicationId, kafkaStreams)
        applicationStreamRegistry.startByApplicationId(applicationId)
    }

    private fun KafkaStreams.logTopologies() =
        metrics().filterKeys { metricName -> metricName.name() == "topology-description" }.values
            .forEach {
                log.info("{}:\n{}", "topology-description", it.metricValue())
            }
}