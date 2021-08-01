package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.ProcessConfigurationMongoRepository
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.transaction.annotation.Transactional

@Transactional
@Retryable(maxAttempts = 5, backoff = Backoff(value = 100))
class ProcessConfigurationMongoAdapter(private val mongoRepository: ProcessConfigurationMongoRepository) : ProcessConfigurationRepository {

    override fun getAll(): List<ProcessConfiguration> = mongoRepository.findAll().map { it.toModel() }

    override fun save(processConfiguration: ProcessConfiguration) {
        mongoRepository.save(processConfiguration.toDao())
    }

    override fun getBy(id: ProcessConfigurationId): ProcessConfiguration? {
        return mongoRepository.findById(id.value).map { it.toModel() }.orElse(null)
    }
}