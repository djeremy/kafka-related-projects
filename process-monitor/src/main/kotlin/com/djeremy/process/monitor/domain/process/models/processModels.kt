package com.djeremy.process.monitor.domain.process.models

import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*

inline class StepId(val value: String)
inline class ProcessConfigurationId(val value: String)
inline class ProcessInstanceId(val value: String)

data class ProcessConfigurationWithSteps(
    val process: ProcessConfiguration,
    val steps: List<StepConfigurationModel>
)

data class ProcessConfiguration(
    val id: ProcessConfigurationId,
    val description: String,
    val duration: Duration
)

data class Step(
    val id: String,
    val configurationId: ProcessConfigurationId,
    val stepId: StepId,
    val eventId: String,
    val receivedAt: LocalDateTime,
    val references: List<Reference>,
    val isLast: Boolean
) {

    var isNewlyAssigned: Boolean? = null
        private set
    var processInstanceId: ProcessInstanceId? = null
        private set

    fun assignNewInstanceId(processInstanceId: ProcessInstanceId) {
        require(this.processInstanceId == null) { "You cannot reassign processInstanceId as one already was assigned" }
        this.processInstanceId = processInstanceId
        this.isNewlyAssigned = true
    }

    fun assignOldInstanceId(processInstanceId: ProcessInstanceId) {
        require(this.processInstanceId == null) { "You cannot reassign processInstanceId as one already was assigned" }
        this.processInstanceId = processInstanceId
        this.isNewlyAssigned = false
    }

    fun mature() {
        require(processInstanceId != null) { "You cannot admit not yet assigned step" }
        isNewlyAssigned = false
    }

    fun getProcessInstance(): ProcessInstance? = processInstanceId?.let { ProcessInstance(it, configurationId) }

    companion object {
        fun newProcessInstanceId(): ProcessInstanceId = ProcessInstanceId(UUID.randomUUID().toString())
    }
}

data class ProcessInstance(val id: ProcessInstanceId, val configurationId: ProcessConfigurationId)

data class ProcessInstanceState(
    val instance: ProcessInstance,
    val steps: List<StepView>,
    val startedAt: LocalDateTime,
    val stages: List<ProcessInstanceStages>,
    val version: Int = 0
) {

    fun setSteps(newSteps: List<StepView>): ProcessInstanceState = ProcessInstanceState(
        instance, newSteps, startedAt, stages, version
    )

    fun finish(): ProcessInstanceState = ProcessInstanceState(
        instance, steps, startedAt, stages + FINISHED, version
    )

    fun admit(): ProcessInstanceState = ProcessInstanceState(
        instance, steps, startedAt, stages + ADMITTED, version
    )

    fun isFinished(): Boolean = stages.contains(FINISHED)

    fun isAdmitted(): Boolean = stages.contains(ADMITTED)

    companion object {
        fun createNew(instance: ProcessInstance, stepViews: List<StepView>) = ProcessInstanceState(
            instance, stepViews, now(), listOf(NEW)
        )
    }
}

enum class ProcessInstanceStages {
    NEW, FINISHED, ADMITTED
}

data class StepView(
    val id: String,
    val stepId: StepId,
    val eventId: String,
    val receivedAt: LocalDateTime,
    val references: List<Reference>
) {

    fun formatAccordingTo(stepConfiguration: StepConfigurationModel): String {
        val referencesFormatted = references.joinToString("], [", "[", "]") {
            "refId=\"${it.referenceId}\", refName=\"${it.referenceName}\""
        }
        return """
                    |Step[
                    |    eventId=${eventId}"
                    |    description="${stepConfiguration.getDescription()}"
                    |    receivedAt="${receivedAt.format(DateTimeFormatter.ISO_DATE_TIME)}
                    |    references=${referencesFormatted}
                    |]
                """.trimMargin("|")
    }
}

data class Reference(
    val referenceId: String,
    val referenceName: String = "Not implemented yet"
)

inline class ProcessInstanceStateProjection(
    val processInstance: ProcessInstance
)