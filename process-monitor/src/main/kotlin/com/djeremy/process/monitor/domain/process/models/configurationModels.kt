package com.djeremy.process.monitor.domain.process.models

import com.djeremy.process.monitor.domain.process.extractFieldStringFrom
import org.apache.avro.generic.GenericRecord
import java.util.UUID

interface StepConfigurationModel {

    fun generateNewId(): String

    fun getTopic(): String

    fun getId(): StepId

    fun getDescription(): String?

    fun getConfigurationId(): ProcessConfigurationId

    fun shouldAccept(event: GenericRecord): Boolean

    fun getEventId(key: String, event: GenericRecord): String

    fun getReferences(event: GenericRecord): List<Reference>

    fun isFirst(): Boolean

    fun hasLastCondition(): Boolean

    // in the future we can think about some more complex process
    // of indicating whether step is last or not.
    fun checkIfLast(event: GenericRecord): Boolean
}

abstract class AbstractStepModel(
        private val id: StepId,
        private val configurationId: ProcessConfigurationId,
        private val description: String?,
        private val topic: String,
        val schemaName: String,
        val eventIdSchemaPath: String?,
        val referenceIdSchemaPaths: List<String> = emptyList(),
        private val isFirst: Boolean,
        val isLast: Boolean
) : StepConfigurationModel {

    override fun generateNewId(): String = UUID.randomUUID().toString()

    override fun getTopic(): String = topic

    override fun getId(): StepId = id

    override fun getDescription(): String? = description

    override fun getConfigurationId(): ProcessConfigurationId = configurationId

    override fun shouldAccept(event: GenericRecord): Boolean = event.schema.fullName == schemaName

    override fun getEventId(key: String, event: GenericRecord): String = extractFieldStringFrom(eventIdSchemaPath, key, event)

    // this probably not the best place for throwing this exception but
    // currently I don't have clear vision where to put it.
    override fun getReferences(event: GenericRecord): List<Reference> = referenceIdSchemaPaths
            .map { Reference(extractFieldStringFrom(it, event)!!, it) }

    override fun isFirst(): Boolean = isFirst

    override fun checkIfLast(event: GenericRecord): Boolean = shouldAccept(event) && isLast

    override fun hasLastCondition(): Boolean = isLast

    @Suppress("EqualsOrHashCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractStepModel) return false

        if (configurationId != other.configurationId) return false
        if (description != other.description) return false
        if (topic != other.topic) return false
        if (schemaName != other.schemaName) return false
        if (eventIdSchemaPath != other.eventIdSchemaPath) return false
        if (referenceIdSchemaPaths != other.referenceIdSchemaPaths) return false
        if (isFirst != other.isFirst) return false
        if (isLast != other.isLast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = configurationId.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + topic.hashCode()
        result = 31 * result + schemaName.hashCode()
        result = 31 * result + eventIdSchemaPath.hashCode()
        result = 31 * result + referenceIdSchemaPaths.hashCode()
        result = 31 * result + isFirst.hashCode()
        result = 31 * result + isLast.hashCode()
        return result
    }
}

@Suppress("EqualsOrHashCode")
open class SingleStepConfigurationModel(
        id: StepId,
        configurationId: ProcessConfigurationId,
        description: String? = null,
        topic: String,
        schemaName: String,
        eventIdSchemaPath: String? = null,
        referenceIdSchemaPaths: List<String> = emptyList(),
        isFirst: Boolean,
        isLast: Boolean
) : AbstractStepModel(id, configurationId, description, topic, schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SingleStepConfigurationModel) return false
        if (!super.equals(other)) return false
        return true
    }
}

open class MultipleExclusiveStepConfigurationModel(
        id: StepId,
        configurationId: ProcessConfigurationId,
        description: String? = null,
        topic: String,
        schemaName: String,
        eventIdSchemaPath: String? = null,
        referenceIdSchemaPaths: List<String> = emptyList(),
        isFirst: Boolean,
        isLast: Boolean,
        val alternativeSchemaName: String,
        val alternativeEventIdSchemaPath: String? = null,
        val alternativeReferenceIdsSchemaPaths: List<String> = emptyList(),
        val alternativeIsLast: Boolean
) : SingleStepConfigurationModel(id, configurationId, description, topic, schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast) {

    private fun isAlternative(event: GenericRecord): Boolean = event.schema.fullName == alternativeSchemaName

    override fun shouldAccept(event: GenericRecord): Boolean = super.shouldAccept(event) || isAlternative(event)

    override fun getEventId(key: String, event: GenericRecord): String {
        return if (isAlternative(event)) extractFieldStringFrom(alternativeEventIdSchemaPath, key, event)
        else super.getEventId(key, event)
    }

    // this probably not the best place for throwing this exception but
    // currently I don't have clear vision where to put it.
    override fun getReferences(event: GenericRecord): List<Reference> {
        return if (isAlternative(event)) alternativeReferenceIdsSchemaPaths
                .map { Reference(extractFieldStringFrom(it, event)!!, it) }
        else super.getReferences(event)
    }

    override fun checkIfLast(event: GenericRecord): Boolean {
        return if (isAlternative(event)) alternativeIsLast
        else super.checkIfLast(event)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipleExclusiveStepConfigurationModel) return false
        if (!super.equals(other)) return false

        if (alternativeSchemaName != other.alternativeSchemaName) return false
        if (alternativeEventIdSchemaPath != other.alternativeEventIdSchemaPath) return false
        if (alternativeReferenceIdsSchemaPaths != other.alternativeReferenceIdsSchemaPaths) return false
        if (alternativeIsLast != other.alternativeIsLast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + alternativeSchemaName.hashCode()
        result = 31 * result + alternativeEventIdSchemaPath.hashCode()
        result = 31 * result + alternativeReferenceIdsSchemaPaths.hashCode()
        result = 31 * result + alternativeIsLast.hashCode()
        return result
    }
}