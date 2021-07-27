package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessInstance
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.StepView
import mu.KotlinLogging.logger
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime.now

interface ProcessInstanceExpiredTask {
    fun execute()
}

open class DefaultProcessInstanceExpiredTask(
        private val processInstanceStateRepository: ProcessInstanceStateRepository,
        private val processConfigurationRepository: ProcessConfigurationRepository,
        private val alert: ProcessExpiredAlert,
        private val lagTime: Duration,
        private val pageSize: Int) : ProcessInstanceExpiredTask {

    val logger = logger {}

    override fun execute() {
        val beforeOffset = now().minus(lagTime)

        val configurationsCache: MutableMap<ProcessConfigurationId, ProcessConfiguration?> =
                mutableMapOf()

        val notAdmittedSteps = processInstanceStateRepository.getNotAdmittedIdsBefore(beforeOffset)

        notAdmittedSteps.chunked(pageSize).forEach { projections ->
            admitAndAlert(projections.map { it.processInstance }, configurationsCache)
        }
    }

    @Transactional
    open fun admitAndAlert(data: List<ProcessInstance>, configurationCache: MutableMap<ProcessConfigurationId, ProcessConfiguration?>) {
        val currentDate = now()

        val processesToAdmit = mutableListOf<ProcessInstanceState>()
        val processesToAlert = mutableListOf<ProcessInstanceState>()
        val admitAndAdd: (ProcessInstanceState) -> Unit = { processesToAdmit.add(it.admit()) }

        processInstanceStateRepository.getBy(data).forEach {
            val processConfiguration = configurationCache.tryGetOrNull(it.instance.configurationId)

            if (processConfiguration != null) {
                val stepsAscending = it.steps.sortedBy(StepView::receivedAt)

                val expectedToFinish = stepsAscending.first().receivedAt.plus(processConfiguration.duration)

                // I believe this can be written much easier
                if (!currentDate.isBefore(expectedToFinish) || it.isFinished()) {
                    admitAndAdd(it)

                    val lastStepDate = stepsAscending.last().receivedAt
                    if (!it.isFinished() || lastStepDate.isAfter(expectedToFinish)) {
                        processesToAlert.add(it)
                    }
                }
            } else {
                logger.warn { "Cannot find process configuration with id [${it.instance.configurationId}]" }
                admitAndAdd(it)
            }
        }
        processInstanceStateRepository.saveAll(processesToAdmit)
        alert.alertOn(processesToAlert)
    }

    private fun MutableMap<ProcessConfigurationId, ProcessConfiguration?>.tryGetOrNull(processConfigurationId: ProcessConfigurationId): ProcessConfiguration? =
            getOrPut(processConfigurationId) { processConfigurationRepository.getBy(processConfigurationId) }
}