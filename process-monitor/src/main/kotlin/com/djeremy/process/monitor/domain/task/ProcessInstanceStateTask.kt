package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.process.ProcessInstanceStateAggregator
import com.djeremy.process.monitor.domain.process.StepService
import mu.KotlinLogging.logger

interface ProcessInstanceStateTask {

    fun execute()
}

class DefaultProcessInstanceStateTask(
    private val processInstanceStateAggregator: ProcessInstanceStateAggregator,
    private val stepService: StepService
) : ProcessInstanceStateTask {

    val logger = logger {}

    override fun execute() {
        val newlyAssignedSteps = stepService.getNewlyAssignedSteps()
        logger.info { "Trying to join steps (${newlyAssignedSteps.size})" }
        processInstanceStateAggregator.aggregate(newlyAssignedSteps)
        stepService.admitAssignedSteps(newlyAssignedSteps)
    }
}