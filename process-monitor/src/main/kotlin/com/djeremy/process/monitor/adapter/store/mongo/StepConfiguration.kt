package com.djeremy.process.monitor.adapter.store.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

const val stepConfigurationDaoColName = "step_configuration"

@Document(stepConfigurationDaoColName)
@TypeAlias("single")
open class StepConfigurationDao(
        @Id
        var id: String = UUID.randomUUID().toString(),
        val configurationId: String,
        val description: String? = null,
        val topic: String,
        val isFirst: Boolean = false,
        val schemaName: String,
        val eventIdSchemaPath: String? = null,
        val referenceIdSchemaPaths: List<String> = emptyList(),
        val isLast: Boolean = false
)

@TypeAlias("multiExclusive")
class MultiExclusiveStepConfigurationDao(
        id: String = UUID.randomUUID().toString(),
        configurationId: String,
        description: String? = null,
        topic: String,
        schemaName: String,
        eventIdSchemaPath: String? = null,
        referenceIdSchemaPaths: List<String> = emptyList(),
        isFirst: Boolean = false,
        isLast: Boolean = false,
        val alternativeSchemaName: String,
        val alternativeEventIdSchemaPath: String? = null,
        val alternativeReferenceIdsSchemaPaths: List<String> = emptyList(),
        val alternativeIsLast: Boolean = false
) : StepConfigurationDao(id, configurationId, description, topic, isFirst, schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isLast)

interface StepConfigurationMongoRepository : MongoRepository<StepConfigurationDao, String>{
        fun findByConfigurationId(configurationId: String): List<StepConfigurationDao>
}

// TODO think about inheritance? :)
// in worst case can be applied: https://medium.com/@mladen.maravic/spring-data-mongodb-my-take-on-inheritance-support-102361c08e3d
interface ExclusiveMultiStepConfigurationDaoV2Repository : MongoRepository<MultiExclusiveStepConfigurationDao, String>