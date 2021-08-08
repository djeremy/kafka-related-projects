package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.*

interface ProcessExpiredAlert {
    fun alertOn(processes: List<ProcessInstanceState>)
}

class ProcessExpiredFormatToStringAlert(
    private val processConfigurationRepository: ProcessConfigurationRepository,
    private val stepConfigurationRepository: StepConfigurationRepository,
    private val ifFinished: (String) -> Unit,
    private val ifNotFinished: (String) -> Unit
) : ProcessExpiredAlert {

    override fun alertOn(processes: List<ProcessInstanceState>) {
        val cache: MutableMap<ProcessConfigurationId, CachedProcess> = mutableMapOf()
        processes.forEach {
            val cachedInstance = cache.getOrCache(it.instance.configurationId)

            if (it.isFinished()) {
                ifFinished(
                    "[Process configuration with ${cachedInstance.formatConfiguration()} -> Process finished but exceeded" +
                            " expected time window. ProcessInstanceId [${it.instance.id}]. Please review steps manually:" +
                            "\n ${it.steps.joinToString(separator = "\n", transform = cachedInstance::formatStep)}"
                )
            } else {
                ifNotFinished(
                    "[Process configuration with ${cachedInstance.formatConfiguration()} -> Process has not finished in " +
                            "expected time window. ProcessInstanceId [${it.instance.id}]. Please review steps manually:" +
                            "\n ${it.steps.joinToString(separator = "\n", transform = cachedInstance::formatStep)}"
                )
            }
        }
    }

    private fun MutableMap<ProcessConfigurationId, CachedProcess>.getOrCache(configurationId: ProcessConfigurationId): CachedProcess =
        getOrPut(configurationId) {
            CachedProcess(
                processConfigurationRepository.getBy(configurationId)!!,
                stepConfigurationRepository.getById(configurationId)
            )
        }

    private class CachedProcess(
        private val processConfiguration: ProcessConfiguration,
        private val steps: List<StepConfigurationModel>
    ) {
        val stepsById: Map<StepId, StepConfigurationModel> = steps.map { it.getId() to it }.toMap()

        fun formatConfiguration(): String =
            "ConfigurationId=\"${processConfiguration.id}\" and ConfigurationDescription=\"${processConfiguration.description}\""

        fun formatStep(stepView: StepView): String = stepView.formatAccordingTo(stepsById[stepView.stepId]!!)
    }
}
