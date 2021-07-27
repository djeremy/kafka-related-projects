package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.process.models.ProcessInstance
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState.Companion.createNew
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.process.models.StepView

interface ProcessInstanceStateService {
    fun aggregate(model: Step)
    fun aggregate(models: List<Step>)
}

class DefaultProcessInstanceStateService(
        private val repository: ProcessInstanceStateRepository
) : ProcessInstanceStateService {

    // add result object containing failure to catch possible exceptions and ignore
    override fun aggregate(model: Step) {
        aggregateInternal(listOf(model), model.getProcessInstance()!!)
    }

    // add result object containing failure to catch possible exceptions and ignore
    override fun aggregate(models: List<Step>) {
        models.groupBy { it.processInstanceId }.forEach {
            aggregateInternal(it.value, it.value.first().getProcessInstance()!!)
        }
    }

    private fun aggregateInternal(steps: List<Step>, processInstance: ProcessInstance) {
        val model = repository.getBy(processInstance)
        val hasFinished = steps.any { it.isLast }
        lateinit var modelToSave: ProcessInstanceState

        modelToSave = model?.addSteps(steps.map()) ?: createNew(processInstance, steps.map())

        if (hasFinished) {
            modelToSave = modelToSave.finish()
        }

        repository.save(modelToSave)
    }

    fun List<Step>.map(): List<StepView> = map {
        StepView(it.id, it.stepId, it.eventId, it.receivedAt, it.references)
    }

    private fun ProcessInstanceState.addSteps(newSteps: List<StepView>): ProcessInstanceState {
        val allSteps = steps.toMutableSet().apply { addAll(newSteps) }.toList()
        return setSteps(allSteps)
    }
}