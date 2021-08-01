package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationWithSteps
import org.springframework.transaction.annotation.Transactional

interface ProcessConfigurationService {

    /**
     * Get all Business Process configurations.
     *
     * @return          list of Business Process configurations
     * @see             ProcessConfigurationWithSteps
     */
    fun getAll(): List<ProcessConfigurationWithSteps>

    /**
     * Updates Business Process configuration in storage.
     *
     * @param   model   an domain representation of configuration
     * @throws  AssertionError  If a new configuration doesn't meet validation criteria.
     */
    fun save(model: ProcessConfigurationWithSteps)
}

@Transactional
class DefaultProcessConfigurationService(
        private val processConfigurationRepository: ProcessConfigurationRepository,
        private val stepConfigurationService: StepConfigurationService
) : ProcessConfigurationService {

    override fun save(model: ProcessConfigurationWithSteps) {
        validateModel(model)

        if (shouldUpdate(model.process)) {
            processConfigurationRepository.save(model.process)
        }

        stepConfigurationService.save(model.steps)
    }

    private fun shouldUpdate(model: ProcessConfiguration): Boolean =
            processConfigurationRepository.getBy(model.id)?.let { it != model } ?: true

    private fun validateModel(model: ProcessConfigurationWithSteps) {
        with(model.steps.groupBy { it.getConfigurationId() }) {
            require(size == 1) { "All steps should have only one configurationId" }
            require(keys.first() == model.process.id) { "Steps should have same configurationId as it's configuration model" }
        }
        require(model.steps.any { it.hasLastCondition() }) { "Process [${model.process.id.value}] should have at least one last step" }
    }

    override fun getAll(): List<ProcessConfigurationWithSteps> = processConfigurationRepository.getAll().map {
        ProcessConfigurationWithSteps(it, stepConfigurationService.getBy(it.id))
    }
}