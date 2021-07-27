package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateMongoRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.process.models.ProcessInstance
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStateProjection
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.by
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import java.time.LocalDateTime

class ProcessInstanceStateMongoAdapter(
        private val repository: ProcessInstanceStateMongoRepository
) : ProcessInstanceStateRepository {

    override fun getBy(id: ProcessInstance): ProcessInstanceState? =
            repository.findById(ProcessInstanceDao(id.id.value, id.configurationId.value))
                    .map { it.toModel() }
                    .orElse(null)

    override fun getNotAdmittedIdsBefore(before: LocalDateTime): List<ProcessInstanceStateProjection> {
        val sort = by(Sort.Direction.ASC, ProcessInstanceStateDao::startedAt.name)
        return repository.findAllByStageIsAdmittedFalseAndStartedAtBefore(before, sort)
                .map { it.toModel() }
    }

    override fun getBy(ids: List<ProcessInstance>): List<ProcessInstanceState> =
            repository.findAllById(ids.map { it.toDao() })
                    .map { it.toModel() }

    @Retryable(value = [DataIntegrityViolationException::class], maxAttempts = 2, backoff = Backoff(delay = 100))
    override fun save(processInstanceState: ProcessInstanceState) {
        repository.save(processInstanceState.toDao())
    }

    @Retryable(value = [DataIntegrityViolationException::class], maxAttempts = 2, backoff = Backoff(delay = 100))
    override fun saveAll(processInstanceStates: List<ProcessInstanceState>) {
        repository.saveAll(processInstanceStates.map { it.toDao() })
    }
}