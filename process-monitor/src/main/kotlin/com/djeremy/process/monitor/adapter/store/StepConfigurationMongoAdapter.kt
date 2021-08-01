package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationMongoRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepId
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.transaction.annotation.Transactional

@Transactional
@Retryable(maxAttempts = 5, backoff = Backoff(value = 100))
class StepConfigurationMongoAdapter(private val mongoRepository: StepConfigurationMongoRepository) : StepConfigurationRepository {
    override fun getById(configurationId: ProcessConfigurationId): List<StepConfigurationModel> = mongoRepository.findByConfigurationId(configurationId.value)
            .map { it.toModel() }

    override fun getById(id: StepId): StepConfigurationModel? = mongoRepository.findById(id.value).map { it.toModel() }.orElse(null)

    override fun save(stepConfiguration: StepConfigurationModel) {
        mongoRepository.save(stepConfiguration.toDao())
    }

    override fun saveAll(stepConfigurations: List<StepConfigurationModel>) {
        mongoRepository.saveAll(stepConfigurations.map(StepConfigurationModel::toDao))
    }

    override fun delete(stepConfigurations: List<StepConfigurationModel>) {
        mongoRepository.deleteAll(stepConfigurations.map(StepConfigurationModel::toDao))
    }
}
