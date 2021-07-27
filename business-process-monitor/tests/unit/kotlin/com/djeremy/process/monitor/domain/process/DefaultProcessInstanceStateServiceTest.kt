package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.domain.`given process instance state in NEW stage`
import com.djeremy.process.monitor.domain.`given step from`
import com.djeremy.process.monitor.domain.`given step`
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.FINISHED
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.NEW
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.toView
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class DefaultProcessInstanceStateServiceTest : Spek({
    describe("Test join methods with different scenarios") {
        val repository: ProcessInstanceStateRepository = mockk()
        val service: ProcessInstanceStateService = DefaultProcessInstanceStateService(repository)

        beforeEachTest { clearMocks(repository) }

        it("join single step and instance not exist") {
            // given
            val step = `given step`(withProcessInstanceId = true)
            val capturedInstance = slot<ProcessInstanceState>()

            every { repository.getBy(step.getProcessInstance()!!) } returns null
            every { repository.save(capture(capturedInstance)) } just runs

            // when
            service.aggregate(step)

            // then
            assertSoftly {
                val result = capturedInstance.captured
                it.assertThat(result.stages)
                        .containsOnly(NEW)
                it.assertThat(result.instance)
                        .isEqualTo(step.getProcessInstance()!!)
                it.assertThat(result.steps)
                        .containsOnly(step.toView())
            }
        }
        it("join all when single step and instance not exist") {
            // given
            val step = `given step`(withProcessInstanceId = true)
            val capturedInstance = slot<ProcessInstanceState>()

            every { repository.getBy(step.getProcessInstance()!!) } returns null
            every { repository.save(capture(capturedInstance)) } just runs

            // when
            service.aggregate(listOf(step))

            // then
            assertSoftly {
                val result = capturedInstance.captured
                it.assertThat(result.stages)
                        .containsOnly(NEW)
                it.assertThat(result.instance)
                        .isEqualTo(step.getProcessInstance()!!)
                it.assertThat(result.steps)
                        .containsOnly(step.toView())
            }
        }
        it("join all when single last step and state exists") {
            // given
            val existingStep = `given step`(withProcessInstanceId = true)
            val existingState = `given process instance state in NEW stage`(withSteps = listOf(existingStep))

            val step = `given step from`(existingStep)
            val capturedInstance = slot<ProcessInstanceState>()

            every { repository.getBy(step.getProcessInstance()!!) } returns existingState
            every { repository.save(capture(capturedInstance)) } just runs

            // when
            service.aggregate(listOf(step))

            // then
            assertSoftly {
                val result = capturedInstance.captured
                it.assertThat(result.stages)
                        .containsOnly(NEW)
                it.assertThat(result.instance)
                        .isEqualTo(step.getProcessInstance()!!)
                it.assertThat(result.steps)
                        .hasSize(2)
                        .contains(step.toView())
            }
        }
        it("join all when single step and state exists and ") {
            // given
            val existingStep = `given step`(withProcessInstanceId = true)
            val existingState = `given process instance state in NEW stage`(withSteps = listOf(existingStep))

            val step = `given step from`(existingStep, isLast = true)
            val capturedInstance = slot<ProcessInstanceState>()

            every { repository.getBy(step.getProcessInstance()!!) } returns existingState
            every { repository.save(capture(capturedInstance)) } just runs

            // when
            service.aggregate(listOf(step))

            // then
            assertSoftly {
                val result = capturedInstance.captured
                it.assertThat(result.stages)
                        .containsAll(listOf(NEW, FINISHED))
                it.assertThat(result.instance)
                        .isEqualTo(step.getProcessInstance()!!)
                it.assertThat(result.steps)
                        .hasSize(2)
                        .contains(step.toView())
            }
        }

        it("join when multiple identical steps and state exists should not save duplicates") {
            // given
            val existingStep = `given step`(withProcessInstanceId = true)
            val existingState = `given process instance state in NEW stage`(withSteps = listOf(existingStep))

            val step = `given step from`(existingStep, isLast = true)
            val capturedInstance = slot<ProcessInstanceState>()

            every { repository.getBy(step.getProcessInstance()!!) } returns existingState
            every { repository.save(capture(capturedInstance)) } just runs

            // when
            service.aggregate(listOf(existingStep, step))

            // then
            assertSoftly {
                val result = capturedInstance.captured
                it.assertThat(result.stages)
                        .containsAll(listOf(NEW, FINISHED))
                it.assertThat(result.instance)
                        .isEqualTo(step.getProcessInstance()!!)
                it.assertThat(result.steps)
                        .hasSize(2)
                        .contains(step.toView())
            }
        }
    }
})