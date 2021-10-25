package com.djeremy.process.monitor.adapter.rest

import com.djeremy.process.monitor.domain.process.models.*
import java.time.LocalDateTime

class ProcessInstanceStateDto(
    val processInstanceId: ProcessInstanceId,
    val processConfiguration: ProcessConfigurationDto,
    val steps: List<StepDto>
)

class ProcessConfigurationDto(
    val id: String,
    val description: String
)

fun mapFrom(
    processState: ProcessInstanceState,
    processConfiguration: ProcessConfiguration,
    steps: List<StepConfigurationModel>
): ProcessInstanceStateDto =
    ProcessInstanceStateDto(
        processState.instance.id,
        ProcessConfigurationDto(processConfiguration.id.value, processConfiguration.description),
        processState.steps.map {
            mapFrom(it, steps.find { stepConfiguration -> stepConfiguration.getId() == it.stepId }!!)
        }

    )

class StepDto(
    val eventId: String,
    val stepId: StepId,
    val stepDescription: String,
    val receivedAt: LocalDateTime,
    val references: List<ReferenceDto>
)

fun mapFrom(step: StepView, stepConfiguration: StepConfigurationModel): StepDto = StepDto(
    step.eventId,
    step.stepId,
    stepConfiguration.getDescription() ?: "",
    step.receivedAt,
    step.references.map {
        ReferenceDto(it.referenceId, it.referenceName)
    }
)

class ReferenceDto(
    val id: String,
    val name: String
)