package com.djeremy.process.monitor.domain

import com.github.javafaker.Faker
import com.github.javafaker.service.FakeValuesService
import com.github.javafaker.service.RandomService
import com.djeremy.process.monitor.randomUUID
import com.djeremy.process.monitor.domain.process.models.MultipleExclusiveStepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState.Companion.createNew
import com.djeremy.process.monitor.domain.process.models.Reference
import com.djeremy.process.monitor.domain.process.models.SingleStepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepId
import com.djeremy.process.monitor.domain.process.models.StepView
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.Locale
import java.util.UUID
import kotlin.random.Random.Default.nextInt

fun `given step`(referenceIds: List<String>? = null, configurationId: String? = null, processInstanceId: ProcessInstanceId? = null,
                 withProcessInstanceId: Boolean = false, isNewlyAssigned: Boolean = true, isLast: Boolean = false, receivedAt: LocalDateTime = now()) =
        Step(
                id = UUID.randomUUID().toString(),
                configurationId = ProcessConfigurationId(configurationId ?: fakeValueService.bothify("#####??#?#?###")),
                stepId = StepId(fakeValueService.bothify("#######? ####???")),
                eventId = randomUUID().toString(),
                receivedAt = receivedAt,
                references = referenceIds?.map { Reference(it) } ?: (0..nextInt(2, 10)).map {
                    Reference(randomUUID().toString())
                }, isLast = isLast)
                .apply {
                    if (withProcessInstanceId) {
                        val pI = processInstanceId ?: ProcessInstanceId(randomUUID().toString())
                        if (isNewlyAssigned) assignNewInstanceId(pI)
                        else assignOldInstanceId(pI)
                    }
                }

fun `given step from`(from: Step, isLast: Boolean = false, receivedAt: LocalDateTime = now()) = Step(
        id = UUID.randomUUID().toString(),
        configurationId = from.configurationId,
        stepId = StepId(fakeValueService.bothify("#######? ####???")),
        eventId = randomUUID().toString(),
        receivedAt = receivedAt,
        references = listOf(Reference(from.eventId)),
        isLast = isLast)
        .apply {
            if (from.isNewlyAssigned == true) assignNewInstanceId(from.processInstanceId!!)
            if (from.isNewlyAssigned == false) assignOldInstanceId(from.processInstanceId!!)
        }


fun `given single step configuration`(configurationId: String? = null, stepId: String? = null, isFirst: Boolean = false,
                                      isLast: Boolean = false): StepConfigurationModel = SingleStepConfigurationModel(
        id = StepId(stepId ?: UUID.randomUUID().toString()),
        configurationId = ProcessConfigurationId(configurationId ?: fakeValueService.bothify("#####??#?#?###")),
        description = "description",
        schemaName = fakeValueService.bothify("djeremy.something.#######"),
        topic = "topic",
        eventIdSchemaPath = "id.value",
        isFirst = isFirst,
        isLast = isLast
)

fun `given multiple exclusive step configuration`(configurationId: String? = null, isLast: Boolean = false): StepConfigurationModel = MultipleExclusiveStepConfigurationModel(
        id = StepId(UUID.randomUUID().toString()),
        configurationId = ProcessConfigurationId(configurationId ?: fakeValueService.bothify("#####??#?#?###")),
        description = "description",
        schemaName = fakeValueService.bothify("djeremy.something.#######"),
        topic = "topic",
        eventIdSchemaPath = "id.value",
        isFirst = false,
        isLast = isLast,
        alternativeIsLast = false,
        alternativeSchemaName = fakeValueService.bothify("djeremy.something.Alternative#######")
)

fun Step.toView(): StepView = StepView(id, stepId, eventId, receivedAt, references)

fun `given process configuration`(duration: Duration, configurationId: String? = null): ProcessConfiguration = ProcessConfiguration(
        id = ProcessConfigurationId(configurationId ?: fakeValueService.bothify("#####??#?#?###")),
        description = "description",
        duration = duration
)

fun `given process instance state in NEW stage`(processInstanceId: ProcessInstanceId? = null,
                                                configurationId: String? = null,
                                                withSteps: List<Step> = emptyList()): ProcessInstanceState {
    val step = withSteps.takeIf { it.isNotEmpty() }
            ?: listOf(`given step`(withProcessInstanceId = true, configurationId = configurationId, processInstanceId = processInstanceId))
    return createNew(step.first().getProcessInstance()!!, step.map{it.toView()})
}

fun `given process instance state in FINISHED stage`(processInstanceId: ProcessInstanceId? = null,
                                                     configurationId: String? = null,
                                                     withSteps: List<Step> = emptyList()): ProcessInstanceState {
    val step = withSteps.takeIf { it.isNotEmpty() }
            ?: listOf(`given step`(withProcessInstanceId = true, configurationId = configurationId, processInstanceId = processInstanceId))
    return createNew(step.first().getProcessInstance()!!, step.map{it.toView()}).finish()
}

fun `given process instance state in ADMITTED stage`(processInstanceId: ProcessInstanceId? = null,
                                                     configurationId: String? = null,
                                                     withSteps: List<Step> = emptyList()): ProcessInstanceState {
    val step = withSteps.takeIf { it.isNotEmpty() }
            ?: listOf(`given step`(withProcessInstanceId = true, configurationId = configurationId, processInstanceId = processInstanceId))
    return createNew(step.first().getProcessInstance()!!, step.map{it.toView()}).admit()
}

val fakeValueService = FakeValuesService(Locale("en-GB"), RandomService())
val faker = Faker(Locale("en-GB"))