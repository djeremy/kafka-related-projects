package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.process.ProcessInstanceStateService
import com.djeremy.process.monitor.domain.process.StepService
import mu.KotlinLogging.logger

interface ProcessInstanceStateTask {

    fun execute()
}

class DefaultProcessInstanceStateTask(
        private val processInstanceStateService: ProcessInstanceStateService,
        private val stepService: StepService
) : ProcessInstanceStateTask {

    val logger = logger {}

    override fun execute() {
        val newlyAssignedSteps = stepService.getNewlyAssignedSteps()
        logger.info { "Trying to join steps (${newlyAssignedSteps.size})" }
        processInstanceStateService.aggregate(newlyAssignedSteps)
        stepService.admitAssignedSteps(newlyAssignedSteps)
    }
}