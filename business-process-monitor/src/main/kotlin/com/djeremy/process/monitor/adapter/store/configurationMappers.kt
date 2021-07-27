package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.MultiExclusiveStepConfigurationDaoV2
import com.djeremy.process.monitor.adapter.store.mongo.ProcessConfigurationDao
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationDaoV2
import com.djeremy.process.monitor.domain.process.models.MultipleExclusiveStepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationWithSteps
import com.djeremy.process.monitor.domain.process.models.SingleStepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepId

fun ProcessConfiguration.toDao() = ProcessConfigurationDao(
        id = id.value,
        description = description,
        duration = duration
)

fun ProcessConfigurationDao.toModel() = ProcessConfiguration(
        id = ProcessConfigurationId(id),
        description = description,
        duration = duration
)

fun ProcessConfigurationWithSteps.toDao(): Pair<ProcessConfigurationDao, List<StepConfigurationDaoV2>> {
    val dao = process.toDao()
    val daoSteps = steps.map(StepConfigurationModel::toDao)
    return dao to daoSteps
}

// TODO rewrite this is, as can work wrongly with different order of 'is' statements.
fun StepConfigurationModel.toDao(): StepConfigurationDaoV2 = when (this) {
    is MultipleExclusiveStepConfigurationModel -> this.toDao()
    is SingleStepConfigurationModel -> this.toDao()
    else -> throw IllegalArgumentException("Unsupported type. ProcessStepModel cannot be converted to DAO.")
}

fun SingleStepConfigurationModel.toDao(): StepConfigurationDaoV2 =
        StepConfigurationDaoV2(
                id = getId().value,
                configurationId = getConfigurationId().value,
                description = description,
                topic = getTopic(),
                isFirst = isFirst(),
                schemaName = schemaName,
                eventIdSchemaPath = eventIdSchemaPath,
                referenceIdSchemaPaths = referenceIdSchemaPaths,
                isLast = isLast)

fun MultipleExclusiveStepConfigurationModel.toDao(): StepConfigurationDaoV2 =
        MultiExclusiveStepConfigurationDaoV2(
                id = getId().value,
                configurationId = getConfigurationId().value,
                description = description,
                topic = getTopic(),
                isFirst = isFirst(),
                schemaName = schemaName,
                eventIdSchemaPath = eventIdSchemaPath,
                referenceIdSchemaPaths = referenceIdSchemaPaths,
                isLast = isLast,
                alternativeSchemaName = alternativeSchemaName,
                alternativeEventIdSchemaPath = alternativeEventIdSchemaPath,
                alternativeReferenceIdsSchemaPaths = alternativeReferenceIdsSchemaPaths,
                alternativeIsLast = alternativeIsLast)


fun StepConfigurationDaoV2.toModel(): StepConfigurationModel = when (this) {
    is MultiExclusiveStepConfigurationDaoV2 -> this.toMultiModelTangled()
    else -> this.toModelTangled()
}

private fun StepConfigurationDaoV2.toModelTangled(): SingleStepConfigurationModel = SingleStepConfigurationModel(
        id = StepId(id),
        configurationId = ProcessConfigurationId(configurationId),
        description = description,
        topic = topic,
        schemaName = schemaName,
        eventIdSchemaPath = eventIdSchemaPath,
        referenceIdSchemaPaths = referenceIdSchemaPaths,
        isFirst = isFirst,
        isLast = isLast
)

private fun MultiExclusiveStepConfigurationDaoV2.toMultiModelTangled(): MultipleExclusiveStepConfigurationModel = MultipleExclusiveStepConfigurationModel(
        id = StepId(id),
        configurationId = ProcessConfigurationId(configurationId),
        description = description,
        topic = topic,
        schemaName = schemaName,
        eventIdSchemaPath = eventIdSchemaPath,
        referenceIdSchemaPaths = referenceIdSchemaPaths,
        isFirst = isFirst,
        isLast = isLast,
        alternativeSchemaName = alternativeSchemaName,
        alternativeEventIdSchemaPath = alternativeEventIdSchemaPath,
        alternativeReferenceIdsSchemaPaths = alternativeReferenceIdsSchemaPaths,
        alternativeIsLast = isLast
)
