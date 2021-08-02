package com.djeremy.process.monitor.domain.transformation

import com.djeremy.avro.business.process.monitor.ProcessStep
import com.djeremy.process.monitor.domain.process.models.Reference
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import org.apache.avro.generic.GenericRecord
import java.time.Instant.now
import com.djeremy.avro.business.process.monitor.Reference as AvroReference

class DefaultStepTransformer(
    private val stepConfigurationModel: StepConfigurationModel
) : ConditionalStepTransformer, StepConfigurationModel by stepConfigurationModel {

    // Ideally this method should return Model or ErrorModel, and catch possible exceptions
    // inside that may be thrown inside this method.
    override fun transform(key: String, event: GenericRecord): Pair<String, ProcessStep> {
        val eventId = getEventId(key, event)
        val references = getReferences(event).takeIf { it.isNotEmpty() } ?: listOf(Reference(eventId, "eventId"))
        val isLast = checkIfLast(event)
        val configurationId = getConfigurationId().value

        val processStep = ProcessStep().apply {
            this.setConfigurationId(configurationId)
            this.setStepId(getId().value)
            this.setEventId(eventId)
            this.setReferences(references.map { AvroReference(it.referenceId, it.referenceName) })
            this.setReceivedAt(now())
            this.setIsLast(isLast)
        }

        return getConfigurationId().value to processStep
    }
}