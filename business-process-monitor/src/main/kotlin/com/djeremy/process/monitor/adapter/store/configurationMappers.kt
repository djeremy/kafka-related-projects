package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.MultiExclusiveStepConfigurationDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessConfigurationDao
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationDao
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

fun ProcessConfigurationWithSteps.toDao(): Pair<ProcessConfigurationDao, List<StepConfigurationDao>> {
    val dao = process.toDao()
    val daoSteps = steps.map(StepConfigurationModel::toDao)
    return dao to daoSteps
}

// TODO rewrite this is, as can work wrongly with different order of 'is' statements.
fun StepConfigurationModel.toDao(): StepConfigurationDao = when (this) {
    is MultipleExclusiveStepConfigurationModel -> this.toDao()
    is SingleStepConfigurationModel -> this.toDao()
    else -> throw IllegalArgumentException("Unsupported type. ProcessStepModel cannot be converted to DAO.")
}

fun SingleStepConfigurationModel.toDao(): StepConfigurationDao =
        StepConfigurationDao(
                id = getId().value,
                configurationId = getConfigurationId().value,
                description = description,
                topic = getTopic(),
                isFirst = isFirst(),
                schemaName = schemaName,
                eventIdSchemaPath = eventIdSchemaPath,
                referenceIdSchemaPaths = referenceIdSchemaPaths,
                isLast = isLast)

fun MultipleExclusiveStepConfigurationModel.toDao(): StepConfigurationDao =
        MultiExclusiveStepConfigurationDao(
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


fun StepConfigurationDao.toModel(): StepConfigurationModel = when (this) {
    is MultiExclusiveStepConfigurationDao -> this.toMultiModelTangled()
    else -> this.toModelTangled()
}

private fun StepConfigurationDao.toModelTangled(): SingleStepConfigurationModel = SingleStepConfigurationModel(
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

private fun MultiExclusiveStepConfigurationDao.toMultiModelTangled(): MultipleExclusiveStepConfigurationModel = MultipleExclusiveStepConfigurationModel(
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
