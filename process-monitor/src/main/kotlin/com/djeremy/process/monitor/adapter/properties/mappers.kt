package com.djeremy.process.monitor.adapter.properties

import com.djeremy.process.monitor.domain.process.models.MultipleExclusiveStepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationWithSteps
import com.djeremy.process.monitor.domain.process.models.SingleStepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepId
import java.util.UUID.randomUUID

fun ProcessConfigurationPropertiesV2.toModelWithSteps() = ProcessConfigurationWithSteps(
        toModel(),
        convertedSteps.mapIndexed { index, it -> it.toModel(id!!, index == 0, ::generateNewStepId) }
)

fun ProcessConfigurationPropertiesV2.toModel() = ProcessConfiguration(
        id = ProcessConfigurationId(id!!),
        description = description!!,
        duration = expectToFinishIn!!
)

fun StepConfigurationPropertiesV2.toModel(configurationId: String, isFirst: Boolean, generateNewId: () -> StepId): StepConfigurationModel = when (this) {
    is SingleStepConfiguration -> this.toModel(configurationId, isFirst, generateNewId)
    is MultipleExclusiveStepConfiguration -> this.toModel(configurationId, isFirst, generateNewId)
    else -> throw IllegalArgumentException("Unsupported type. ProcessStepModel cannot be converted to StepConfigurationModel")
}

fun SingleStepConfiguration.toModel(configurationId: String, isFirst: Boolean, getId: () -> StepId): SingleStepConfigurationModel =
        SingleStepConfigurationModel(
                id = getId(),
                configurationId = ProcessConfigurationId(configurationId),
                description = description,
                topic = topic!!,
                schemaName = schemaName!!,
                eventIdSchemaPath = eventIdSchemaPath,
                referenceIdSchemaPaths = referenceIdSchemaPaths,
                isFirst = isFirst,
                isLast = indicateProcessFinished
        )

// think about abstraction of generating stepId, as may be unreadable.
fun MultipleExclusiveStepConfiguration.toModel(configurationId: String, isFirst: Boolean, generateNewId: () -> StepId): MultipleExclusiveStepConfigurationModel =
        MultipleExclusiveStepConfigurationModel(
                id = generateNewId(),
                configurationId = ProcessConfigurationId(configurationId),
                description = description,
                topic = topic!!,
                schemaName = schemaName!!,
                eventIdSchemaPath = eventIdSchemaPath,
                referenceIdSchemaPaths = referenceIdSchemaPaths,
                alternativeSchemaName = altSchemaName!!,
                alternativeEventIdSchemaPath = altEventIdSchemaPath,
                alternativeReferenceIdsSchemaPaths = altReferenceIdSchemaPaths,
                isFirst = isFirst,
                isLast = indicateProcessFinished,
                alternativeIsLast = altIndicateProcessFinished
        )

fun generateNewStepId() = StepId(randomUUID().toString())