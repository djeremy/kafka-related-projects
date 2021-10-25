package com.djeremy.process.monitor.adapter.store.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

const val processInstanceDaoColName = "process_instance_state"

@Document(value = processInstanceDaoColName)
data class ProcessInstanceStateDao(
    @Id
    val instance: ProcessInstanceDao?,
    val steps: List<StepViewDao>?,
    val startedAt: LocalDateTime?,
    val stage: ProcessInstanceStage?,
    val version: Int?
)

data class ProcessInstanceStage(
    var isFinished: Boolean = false,
    var isAdmitted: Boolean = false
)

data class StepViewDao(
    val id: String,
    val stepId: String,
    val eventId: String,
    val receivedAt: LocalDateTime,
    val references: List<ReferenceDao>
)

data class ReferenceDao(
    val referenceId: String,
    val referenceName: String
)

interface ProcessInstanceStateIdProjection {

    fun getInstance(): ProcessInstanceDao
}

interface ProcessInstanceStateMongoRepository : MongoRepository<ProcessInstanceStateDao, ProcessInstanceDao> {

    fun findAllByInstanceConfigurationId(
        processConfigurationId: String,
        pageable: Pageable
    ): Page<ProcessInstanceStateDao>

    fun findAllByStageIsAdmittedFalseAndStartedAtBefore(
        startedAt: LocalDateTime,
        sort: Sort
    ): List<ProcessInstanceStateIdProjection>
}