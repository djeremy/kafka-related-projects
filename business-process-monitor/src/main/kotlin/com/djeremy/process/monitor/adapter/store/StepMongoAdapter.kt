package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.adapter.store.mongo.StepMongoRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import com.djeremy.process.monitor.domain.process.models.Step
import org.springframework.data.domain.PageRequest.of
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.by
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.transaction.annotation.Transactional

@Transactional
@Retryable(maxAttempts = 5, backoff = Backoff(value = 100))
class StepMongoAdapter(private val mongoRepository: StepMongoRepository) : StepRepository {

    companion object {
        const val pageSize = 5000
    }

    override fun save(model: Step) {
        mongoRepository.save(model.toDao())
    }

    override fun saveAll(models: List<Step>) {
        mongoRepository.saveAll(models.map { it.toDao() })
    }

    override fun getNewSteps(): List<Step> {
        val sort = by(Sort.Direction.ASC, StepDao::receivedAt.name)
        return mongoRepository.findByIsNewlyInstanceAssignedTrue(
                pageable = of(0, pageSize, sort)
        ).map { it.toModel() }.content
    }

    override fun getWithProcessInstanceBy(configurationId: String, referenceId: String): List<Step> =
            mongoRepository.findByConfigurationIdEqualsAndProcessInstanceIsNotNullAndReferencesReferenceIdEquals(
                    configurationId = configurationId,
                    referenceId = referenceId
            ).map { it.toModel() }

    override fun getWithoutProcessInstanceBy(configurationId: String, referenceId: String): List<Step> =
            mongoRepository.findByConfigurationIdEqualsAndProcessInstanceIsNullAndReferencesReferenceIdEquals(
                    configurationId = configurationId,
                    referenceId = referenceId
            ).map { it.toModel() }

    override fun getWithoutProcessInstanceBy(configurationId: String, eventIds: List<String>): List<Step> =
            mongoRepository.findByConfigurationIdEqualsAndProcessInstanceIsNullAndEventIdIn(
                    configurationId = configurationId,
                    eventIds = eventIds
            ).map { it.toModel() }
}