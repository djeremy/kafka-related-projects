package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStage
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateIdProjection
import com.djeremy.process.monitor.adapter.store.mongo.ReferenceDao
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.adapter.store.mongo.StepViewDao
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessInstance
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.ADMITTED
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.FINISHED
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.NEW
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStateProjection
import com.djeremy.process.monitor.domain.process.models.Reference
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.process.models.StepId
import com.djeremy.process.monitor.domain.process.models.StepView

fun Step.toDao(): StepDao = StepDao(id, configurationId.value, stepId.value,
        eventId, receivedAt, references.map { it.toDao() }, isLast,
        processInstanceId?.let { ProcessInstanceDao(it.value, configurationId.value) }, isNewlyAssigned)

fun StepDao.toModel(): Step {
    val model = Step(id, ProcessConfigurationId(configurationId), StepId(stepId),
            eventId, receivedAt, references.map { it.toModel() }, isLast ?: true)

    processInstance?.also {
        if (isNewlyInstanceAssigned == true) {
            model.assignNewInstanceId(ProcessInstanceId(it.id))
        }
        if (isNewlyInstanceAssigned == false) {
            model.assignOldInstanceId(ProcessInstanceId(it.id))
        }
    }
    return model
}

fun ProcessInstanceState.toDao(): ProcessInstanceStateDao = ProcessInstanceStateDao(
        instance.toDao(),
        steps.map { it.toDao() },
        startedAt,
        stages.createStage(),
        version
)

fun ProcessInstance.toDao(): ProcessInstanceDao = ProcessInstanceDao(id.value, configurationId.value)

fun StepView.toDao(): StepViewDao = StepViewDao(id, stepId.value, eventId, receivedAt,
        references.map { it.toDao() })

fun Reference.toDao(): ReferenceDao = ReferenceDao(referenceId, referenceName)

fun List<ProcessInstanceStages>.createStage(): ProcessInstanceStage {
    val empty = ProcessInstanceStage()
    val applyStage: ProcessInstanceStages.() -> Unit = {
        if (this == FINISHED) empty.isFinished = true
        if (this == ADMITTED) empty.isAdmitted = true
    }
    forEach(applyStage)
    return empty
}

fun ProcessInstanceStateDao.toModel(): ProcessInstanceState = ProcessInstanceState(
        instance!!.toModel(),
        steps!!.map { it.toModel() },
        startedAt!!,
        stage!!.toStages(),
        version!!
)

fun ProcessInstanceDao.toModel(): ProcessInstance = ProcessInstance(
        ProcessInstanceId(id), ProcessConfigurationId(configurationId))

fun StepViewDao.toModel(): StepView = StepView(id, StepId(stepId), eventId, receivedAt,
        references.map { it.toModel() })

fun ReferenceDao.toModel(): Reference = Reference(referenceId, referenceName)

fun ProcessInstanceStage.toStages(): List<ProcessInstanceStages> {
    val stages = mutableListOf(NEW)
    if (isAdmitted) stages.add(ADMITTED)
    if (isFinished) stages.add(FINISHED)
    return stages
}

fun ProcessInstanceStateIdProjection.toModel(): ProcessInstanceStateProjection = ProcessInstanceStateProjection(getInstance().toModel())