package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.*
import mu.KotlinLogging

interface ProcessExpiredAlert {
    fun alertOn(processes: List<ProcessInstanceState>)
}

class ProcessExpiredFormatToStringAlert(
    private val processConfigurationRepository: ProcessConfigurationRepository,
    private val stepConfigurationRepository: StepConfigurationRepository,
    private val ifFinished: (String) -> Unit,
    private val ifNotFinished: (String) -> Unit
) : ProcessExpiredAlert {

    val logger = KotlinLogging.logger {}

    override fun alertOn(processes: List<ProcessInstanceState>) {
        val cache: MutableMap<ProcessConfigurationId, CachedProcess> = mutableMapOf()
        processes.forEach { instanceToAlert ->
            kotlin.runCatching {
                val cachedInstance = cache.getOrCache(instanceToAlert.instance.configurationId)

                if (instanceToAlert.isFinished()) {
                    ifFinished(
                        "[Process configuration with ${cachedInstance.formatConfiguration()} -> Process finished but exceeded" +
                                " expected time window. ProcessInstanceId [${instanceToAlert.instance.id}]. Please review steps manually:" +
                                "\n ${
                                    instanceToAlert.steps.joinToString(
                                        separator = "\n",
                                        transform = cachedInstance::formatStep
                                    )
                                }"
                    )
                } else {
                    ifNotFinished(
                        "[Process configuration with ${cachedInstance.formatConfiguration()} -> Process has not finished in " +
                                "expected time window. ProcessInstanceId [${instanceToAlert.instance.id}]. Please review steps manually:" +
                                "\n ${
                                    instanceToAlert.steps.joinToString(
                                        separator = "\n",
                                        transform = cachedInstance::formatStep
                                    )
                                }"
                    )
                }
            }.onFailure {
                logger.error(it) { "Failure to alert $instanceToAlert" }
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
