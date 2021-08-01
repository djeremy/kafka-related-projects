package com.djeremy.process.monitor.adapter.store

import com.djeremy.process.monitor.v2._extracting
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStage
import com.djeremy.process.monitor.adapter.store.mongo.ReferenceDao
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.ADMITTED
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.FINISHED
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.NEW
import com.djeremy.process.monitor.domain.process.models.Reference
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.process.models.StepId
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime.now

internal class MappersKtTest : Spek({
    val id = "someId"
    val stepId = StepId("stepId")
    val configurationId = ProcessConfigurationId("configurationId")
    val eventId = "eventId"
    val receivedAt = now()
    val referenceId = "referenceId"
    val referenceName = "referenceName"
    val references = listOf(Reference(referenceId, referenceName))
    val isLast = false

    describe("Test Step toDao method") {
        it("should return dao without process instance id") {
            // given
            val step = Step(id, configurationId, stepId, eventId, receivedAt, references, isLast)

            // when
            val result = step.toDao()

            // then
            assertThat(result)
                    ._extracting(StepDao::id::get, StepDao::stepId::get, StepDao::configurationId::get,
                            StepDao::eventId::get, StepDao::receivedAt::get, StepDao::isLast::get,
                            StepDao::references::get, StepDao::processInstance::get, StepDao::isNewlyInstanceAssigned::get)
                    .containsOnly(
                            id, stepId.value, configurationId.value,
                            eventId, receivedAt, isLast,
                            listOf(ReferenceDao(referenceId, referenceName)), null, null
                    )

        }
        it("should return dao with process instance id") {
            // given
            val step = Step(id, configurationId, stepId, eventId, receivedAt, references, isLast)
            val processInstance = Step.newProcessInstanceId()
            step.assignNewInstanceId(processInstance)

            // when
            val result = step.toDao()

            // then
            assertThat(result)
                    ._extracting(StepDao::processInstance::get, StepDao::isNewlyInstanceAssigned::get)
                    .contains(
                            ProcessInstanceDao(processInstance.value, configurationId.value), true
                    )
        }


    }

    describe("Test StepDao toModel method") {
        it("should return model without process instance id") {
            // given
            val step = StepDao(id, configurationId.value, stepId.value, eventId, receivedAt, references.map { it.toDao() }, isLast)

            // when
            val result = step.toModel()

            // then
            assertThat(result)
                    ._extracting(Step::processInstanceId::get, Step::isNewlyAssigned::get)
                    .containsOnly(null, null)
        }
        it("should return model with process instance id and flag false") {
            // given
            val processInstanceId = ProcessInstanceDao(configurationId = configurationId.value)
            val step = StepDao(id, configurationId.value, stepId.value, eventId, receivedAt, references.map { it.toDao() }, isLast, processInstanceId, false)

            // when
            val result = step.toModel()

            // then
            assertThat(result.processInstanceId to result.isNewlyAssigned)
                    .isEqualTo(ProcessInstanceId(processInstanceId.id) to false)
        }
        it("should return model with process instance id and flag true") {
            // given
            val processInstanceId = ProcessInstanceDao(configurationId = configurationId.value)
            val step = StepDao(id, configurationId.value, stepId.value, eventId, receivedAt, references.map { it.toDao() }, isLast, processInstanceId, true)

            // when
            val result = step.toModel()

            // then
            assertThat(result.processInstanceId to result.isNewlyAssigned)
                    .isEqualTo(ProcessInstanceId(processInstanceId.id) to true)
        }
    }

    describe("Test ProcessInstance toStages method with different scenarios") {

        val params = listOf(
                Pair(ProcessInstanceStage(isFinished = false, isAdmitted = false), listOf(NEW)),
                Pair(ProcessInstanceStage(isFinished = true, isAdmitted = false), listOf(NEW, FINISHED)),
                Pair(ProcessInstanceStage(isFinished = false, isAdmitted = true), listOf(NEW, ADMITTED)),
                Pair(ProcessInstanceStage(isFinished = true, isAdmitted = true), listOf(NEW, FINISHED, ADMITTED))
        )
        params.forEach {

            it("given ${it.first} should contain ${it.second}") {
                // when
                val result = it.first.toStages()
                // then
                assertThat(result)
                        .containsAll(it.second)
            }
        }
    }

    describe("Test createStage method with different scenarios") {

        val params = listOf(
                Pair(listOf(NEW),ProcessInstanceStage(isFinished = false, isAdmitted = false)),
                Pair(listOf(NEW, FINISHED), ProcessInstanceStage(isFinished = true, isAdmitted = false)),
                Pair(listOf(NEW, ADMITTED),ProcessInstanceStage(isFinished = false, isAdmitted = true)),
                Pair(listOf(NEW, FINISHED, ADMITTED), ProcessInstanceStage(isFinished = true, isAdmitted = true))
        )
        params.forEach {

            it("given ${it.first} should be equal to ${it.second}") {
                // when
                val result = it.first.createStage()
                // then
                assertThat(result).isEqualTo(it.second)
            }
        }
    }
})