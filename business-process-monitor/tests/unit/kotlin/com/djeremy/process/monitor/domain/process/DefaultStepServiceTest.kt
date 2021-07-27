package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.`given step`
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

internal class DefaultStepServiceTest : Spek({
    Feature("Step service with different scenarios") {
        val stepRepository = mockk<StepRepository>()
        val stepConfigurationRepository = mockk<StepConfigurationRepository>()
        val service: StepService = DefaultStepService(stepRepository, stepConfigurationRepository)

        afterEachScenario { clearAllMocks() }

        Scenario("Fist step came first") {
            lateinit var stepToProcess: Step
            val updatedSteps = mutableListOf<Step>()

            Given("Init steps and repository mocks") {
                stepToProcess = `given step`()

                every { stepConfigurationRepository.getById(stepToProcess.stepId) } returns mockk {
                    every { isFirst() } returns true
                }
                every { stepRepository.save(capture(updatedSteps)) } just Runs
                every { stepRepository.getWithoutProcessInstanceBy(any(), any<List<String>>()) } returns emptyList()
            }

            When("ProcessStep is called") {
                service.processStep(stepToProcess)
            }

            Then("Should set new instance id") {
                assertThat(updatedSteps).hasSize(1)
                assertThat(updatedSteps.map { it.processInstanceId }.groupBy { it })
                        .doesNotContainKey(null)
                        .hasSize(1)
                assertThat(updatedSteps.map { it.isNewlyAssigned })
                        .containsOnly(true)
            }
        }

        Scenario("Fist step came already after another") {
            lateinit var stepToProcess: Step
            lateinit var stepDescendant: Step
            val updatedSteps = mutableListOf<Step>()

            Given("Init steps and repository mocks") {
                stepToProcess = `given step`()
                stepDescendant = `given step`()

                every { stepConfigurationRepository.getById(stepToProcess.stepId) } returns mockk {
                    every { isFirst() } returns true
                }
                every { stepRepository.save(capture(updatedSteps)) } just Runs
                every { stepRepository.getWithoutProcessInstanceBy(any(), any<List<String>>()) } returns
                        listOf(stepDescendant) andThen emptyList()
            }

            When("ProcessStep is called") {
                service.processStep(stepToProcess)
            }

            Then("Should set same instance id for both steps") {
                assertThat(updatedSteps).hasSize(2)
                assertThat(updatedSteps.map { it.processInstanceId }.groupBy { it })
                        .doesNotContainKey(null)
                        .hasSize(1)
                assertThat(updatedSteps.map { it.isNewlyAssigned })
                        .containsOnly(true)
            }
        }

        Scenario("Any other step came when there was NO ancestors with instance id") {
            lateinit var stepToProcess: Step
            val updatedSteps = mutableListOf<Step>()

            Given("Init steps and repository mocks") {
                stepToProcess = `given step`()

                every { stepConfigurationRepository.getById(stepToProcess.stepId) } returns mockk {
                    every { isFirst() } returns false
                }

                every { stepRepository.save(capture(updatedSteps)) } just Runs
                every { stepRepository.getWithProcessInstanceBy(any(), any()) } returns emptyList()
            }

            When("ProcessStep is called") {
                service.processStep(stepToProcess)
            }

            Then("Should set same instance id for both steps") {
                assertThat(updatedSteps).hasSize(1)
                assertThat(updatedSteps.mapNotNull { it.processInstanceId }.groupBy { it })
                        .hasSize(0)
            }
        }

        Scenario("Any other step came when there was already ancestors (with instanceId) and no descendants") {
            lateinit var stepToProcess: Step
            lateinit var stepAncestorsEmpty: Step
            lateinit var stepAncestors: Step
            val updatedSteps = mutableListOf<Step>()

            Given("Init steps and repository mocks") {
                stepToProcess = `given step`()
                stepAncestors = `given step`(withProcessInstanceId = true)
                stepAncestorsEmpty = `given step`()

                every { stepConfigurationRepository.getById(stepToProcess.stepId) } returns mockk {
                    every { isFirst() } returns false
                }

                every { stepRepository.save(capture(updatedSteps)) } just Runs
                every { stepRepository.getWithProcessInstanceBy(any(), any()) } returns listOf(stepAncestors)

                every { stepRepository.getWithoutProcessInstanceBy(any(), any<List<String>>()) } returns emptyList()
                every { stepRepository.getWithoutProcessInstanceBy(any(), any<String>()) } returns
                        listOf(stepAncestorsEmpty) andThen emptyList()
            }

            When("ProcessStep is called") {
                service.processStep(stepToProcess)
            }

            Then("Should set same instance id for both steps") {
                assertThat(updatedSteps).hasSize(2)
                assertThat(updatedSteps.map { it.processInstanceId }.groupBy { it })
                        .doesNotContainKey(null)
                        .hasSize(1)
                assertThat(updatedSteps.map { it.isNewlyAssigned })
                        .containsOnly(true)
            }
        }

        Scenario("Any other step came when there was already ancestors (with instanceId) and are descendants") {
            lateinit var stepToProcess: Step
            lateinit var stepDescendant: Step
            lateinit var stepAncestorEmpty: Step
            lateinit var stepAncestor: Step
            val updatedSteps = mutableListOf<Step>()

            Given("Init steps and repository mocks") {
                stepToProcess = `given step`()
                stepAncestor = `given step`(withProcessInstanceId = true)
                stepAncestorEmpty = `given step`()
                stepDescendant = `given step`()

                every { stepConfigurationRepository.getById(stepToProcess.stepId) } returns mockk {
                    every { isFirst() } returns false
                }

                every { stepRepository.save(capture(updatedSteps)) } just Runs
                every { stepRepository.getWithProcessInstanceBy(any(), any()) } returns listOf(stepAncestor)

                every { stepRepository.getWithoutProcessInstanceBy(any(), any<List<String>>()) } returns
                        listOf(stepDescendant) andThen emptyList()
                every { stepRepository.getWithoutProcessInstanceBy(any(), any<String>()) } returns
                        listOf(stepAncestorEmpty) andThen emptyList()
            }

            When("ProcessStep is called") {
                service.processStep(stepToProcess)
            }

            Then("Should set same instance id for both steps") {
                assertThat(updatedSteps).hasSize(3)
                assertThat(updatedSteps.map { it.processInstanceId }.groupBy { it })
                        .doesNotContainKey(null)
                        .hasSize(1)
                assertThat(updatedSteps.map { it.isNewlyAssigned })
                        .containsOnly(true)
            }
        }
    }
})