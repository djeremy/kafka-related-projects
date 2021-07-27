package com.djeremy.process.monitor.adapter.store.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime
import java.util.UUID

const val stepDaoColName = "steps_v2"

@Document(collection = stepDaoColName)
open class StepDao(
    @Id
    var id: String = UUID.randomUUID().toString(),
    val configurationId: String,
    val stepId: String,
    val eventId: String, // careful with renaming used in index
    val receivedAt: LocalDateTime, // careful with renaming used in index
    val references: List<ReferenceDao>,
    val isLast: Boolean? = null,
    @Indexed(sparse = true)
    var processInstance: ProcessInstanceDao? = null, // careful with renaming used in index
    var isNewlyInstanceAssigned: Boolean? = null // careful with renaming used in index
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StepDao) return false

        if (id != other.id) return false
        if (configurationId != other.configurationId) return false
        if (stepId != other.stepId) return false
        if (eventId != other.eventId) return false
        if (receivedAt != other.receivedAt) return false
        if (references != other.references) return false
        if (processInstance != other.processInstance) return false
        if (isNewlyInstanceAssigned != other.isNewlyInstanceAssigned) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + stepId.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + receivedAt.hashCode()
        result = 31 * result + references.hashCode()
        result = 31 * result + processInstance.hashCode()
        result = 31 * result + isNewlyInstanceAssigned.hashCode()
        return result
    }

}

data class ProcessInstanceDao(
    val id: String = UUID.randomUUID().toString(),
    val configurationId: String
)

interface StepMongoRepository : MongoRepository<StepDao, String> {

    fun findByIsNewlyInstanceAssignedTrue(pageable: Pageable): Page<StepDao>

    fun findByConfigurationIdEqualsAndProcessInstanceIsNotNullAndReferencesReferenceIdEquals(
        configurationId: String,
        referenceId: String
    ): List<StepDao>

    fun findByConfigurationIdEqualsAndProcessInstanceIsNullAndReferencesReferenceIdEquals(
        configurationId: String,
        referenceId: String
    ): List<StepDao>

    fun findByConfigurationIdEqualsAndProcessInstanceIsNullAndEventIdIn(
        configurationId: String,
        eventIds: List<String>
    ): List<StepDao>
}