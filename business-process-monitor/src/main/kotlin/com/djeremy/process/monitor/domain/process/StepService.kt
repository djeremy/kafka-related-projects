package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import com.djeremy.process.monitor.domain.process.ProcessingStatus.FAIL
import com.djeremy.process.monitor.domain.process.ProcessingStatus.SUCCESS
import com.djeremy.process.monitor.domain.process.StepProcessingResult.Companion.failed
import com.djeremy.process.monitor.domain.process.StepProcessingResult.Companion.success
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceId
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.process.models.Step.Companion.newProcessInstanceId
import com.djeremy.process.monitor.utils.toCause
import org.springframework.transaction.annotation.Transactional

interface StepService {

    fun processStep(step: Step): StepProcessingResult

    fun getNewlyAssignedSteps(): List<Step>

    fun admitAssignedSteps(steps: List<Step>)
}

class StepProcessingResult private constructor(private val status: ProcessingStatus, val reason: String? = null) {

    fun isSucceed(): Boolean = status == SUCCESS

    fun onFail(action: StepProcessingResult.() -> Unit) {
        if (!isSucceed()) this.action()
    }

    companion object {
        fun success(): StepProcessingResult = StepProcessingResult(SUCCESS)
        fun failed(withReason: String): StepProcessingResult = StepProcessingResult(FAIL, withReason)
    }
}

private enum class ProcessingStatus { SUCCESS, FAIL }

@Transactional
class DefaultStepService(
        private val repository: StepRepository,
        private val stepConfigurationRepository: StepConfigurationRepository
) : StepService {
    /**
     * This method tries to join Step with it's ancestors and descendants if possible.
     * Algorithm starts with first Step for Process (this information is taken from StepConfiguration) for
     * setting single process instance id. Subsequently, this id will join together all steps.
     * When step is first, then we try to find all it's descendants and set newly created instance id.
     * Otherwise, we try to find already set instance id from it's ancestors.
     * If it does not exist we simply save new step and finish processing.
     * If it does, then we retrieve process instance from ancestors and try to join descendents and ancestors
     * recursively.
     */
    override fun processStep(step: Step): StepProcessingResult = kotlin
            .runCatching {
                require(step.processInstanceId == null) { "Step should came without process instance id" }

                if (step.isFirst()) {
                    step.joinDescendants(newProcessInstanceId())
                } else {
                    val ancestors = repository
                            .getWithProcessInstanceBy(step.configurationId.value, step.eventId)
                            .filter { it.processInstanceId != null }
                    if (ancestors.isEmpty()) {
                        repository.save(step)
                    } else {
                        val processInstanceId = ancestors.first().processInstanceId!!
                        step.joinDescendants(processInstanceId)
                        step.getNotYetAssignedAncestors().forEach { it.joinAncestors(processInstanceId) }
                    }
                }
            }.map {success() }.getOrElse { failed(it.toCause()) }


    override fun getNewlyAssignedSteps(): List<Step> = repository.getNewSteps()

    override fun admitAssignedSteps(steps: List<Step>) {
        repository.saveAll(steps.map { it.apply { mature() } })
    }

    private fun Step.joinDescendants(newProcessInstanceId: ProcessInstanceId) {
        if (processInstanceId == null) {
            assignNewInstanceId(newProcessInstanceId)
            repository.save(this)
            val childSteps = getNotYetAssignedDescendants()
            childSteps.forEach { it.joinDescendants(newProcessInstanceId) }
        }
    }

    private fun Step.joinAncestors(newProcessInstanceId: ProcessInstanceId) {
        if (processInstanceId == null) {
            assignNewInstanceId(newProcessInstanceId)
            repository.save(this)
            val parents = getNotYetAssignedAncestors()
            parents.forEach { it.joinAncestors(newProcessInstanceId) }
        }
    }

    private fun Step.getNotYetAssignedAncestors(): List<Step> = repository.getWithoutProcessInstanceBy(configurationId.value, eventId)
    private fun Step.getNotYetAssignedDescendants(): List<Step> = repository.getWithoutProcessInstanceBy(configurationId.value, references.map { it.referenceId })

    private fun Step.isFirst(): Boolean = stepConfigurationRepository.getById(stepId)?.isFirst()
            ?: throw RuntimeException("Cannot find Configuration for Id $stepId")
}