package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import org.springframework.transaction.annotation.Transactional

@Transactional
interface StepConfigurationService {

    fun getBy(configurationId: ProcessConfigurationId): List<StepConfigurationModel>

    /**
     * Updates Step configurations for single ProcessConfiguration.
     *
     * @param   models and domain representation of step configuration.
     * @throws  AssertionError  If steps belong to not a single ProcessConfiguration.
     */
    fun saveForSingleProcessConfiguration(models: List<StepConfigurationModel>)
}

class DefaultStepConfigurationService(
    private val stepConfigurationRepository: StepConfigurationRepository
) : StepConfigurationService {

    override fun getBy(configurationId: ProcessConfigurationId): List<StepConfigurationModel> =
        stepConfigurationRepository.getById(configurationId)

    override fun saveForSingleProcessConfiguration(models: List<StepConfigurationModel>) {
        validateModel(models)

        val saved = stepConfigurationRepository.getById(models.first().getConfigurationId())

        if (shouldUpdate(models, saved)) {
            if (saved.isNotEmpty()) stepConfigurationRepository.delete(saved)
            stepConfigurationRepository.saveAll(models)
        }
    }

    private fun validateModel(models: List<StepConfigurationModel>) {
        with(models.groupBy { it.getConfigurationId() }) {
            require(size == 1) { "All steps should have only one configurationId" }
        }
        require(models.toSet().size == models.size) { "Steps should be unique" }
    }

    private fun shouldUpdate(models: List<StepConfigurationModel>, saved: List<StepConfigurationModel>): Boolean {
        return models.size != saved.size || !saved.containsAll(models)
    }
}