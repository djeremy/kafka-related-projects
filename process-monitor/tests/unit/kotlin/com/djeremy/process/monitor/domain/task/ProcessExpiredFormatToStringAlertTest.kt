package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.*
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.*
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.time.Duration

class ProcessExpiredFormatToStringAlertTest : Spek({

    Feature("Test logging process expired alert with different scenarios") {
        afterEachScenario { clearAllMocks() }

        val processConfigurationRepository: ProcessConfigurationRepository by memoized { mockk<ProcessConfigurationRepository>() }
        val stepConfigurationRepository: StepConfigurationRepository by memoized { mockk<StepConfigurationRepository>() }
        val isFinishedCallback by memoized<(String) -> Unit> { mockk() }
        val isNotFinishedCallback by memoized<(String) -> Unit> { mockk() }
        val alert by memoized {
            ProcessExpiredFormatToStringAlert(
                processConfigurationRepository,
                stepConfigurationRepository,
                isFinishedCallback,
                isNotFinishedCallback
            )
        }

        Scenario("When process NOT finished") {
            lateinit var configurationId: String
            lateinit var givenStepConfigurations: List<StepConfigurationModel>
            lateinit var givenProcessConfiguration: ProcessConfiguration
            lateinit var givenStepOne: Step
            lateinit var givenProcessInstanceId: String
            lateinit var givenProcessInstance: ProcessInstanceState

            Given("Configuration id") {
                configurationId = "configurationId"
            }
            Given("Step and process instance with stepOne only") {
                givenProcessInstanceId = "givenProcessInstanceId"
                givenStepOne = `given step`(
                    configurationId = configurationId,
                    withProcessInstanceId = true,
                    processInstanceId = ProcessInstanceId(givenProcessInstanceId)
                )
                givenProcessInstance = `given process instance state in NEW stage`(
                    processInstanceId = ProcessInstanceId(givenProcessInstanceId),
                    configurationId = configurationId,
                    withSteps = listOf(givenStepOne)
                )
            }
            Given("Process configuration with two steps") {
                givenStepConfigurations = listOf(
                    `given single step configuration`(
                        configurationId = configurationId,
                        stepId = givenStepOne.stepId.value,
                        isFirst = true
                    ),
                    `given single step configuration`(configurationId, isLast = true)
                )

                givenProcessConfiguration = `given process configuration`(Duration.ZERO, configurationId)
            }

            Given("ProcessConfigurationRepository return givenProcessConfiguration") {
                every { processConfigurationRepository.getBy(ProcessConfigurationId(configurationId)) } returns givenProcessConfiguration
            }
            Given("StepConfigurationRepository return givenStepConfigurations") {
                every { stepConfigurationRepository.getById(ProcessConfigurationId(configurationId)) } returns givenStepConfigurations
            }

            lateinit var isNotFinishedAlertedString: MutableList<String>
            Given("Is finished callback captured") {
                isNotFinishedAlertedString = mutableListOf()
                every { isNotFinishedCallback.invoke(capture(isNotFinishedAlertedString)) } just runs
            }

            When("Alert is triggered on givenProcessInstance") {
                alert.alertOn(listOf(givenProcessInstance))
            }

            Then("Not finished alert callback should be triggered with properly formatted string") {
                assertThat(isNotFinishedAlertedString).isNotEmpty
                assertThat(isNotFinishedAlertedString.first()).contains(
                    "Process has not finished in",
                    "expected time window",
                    "Please review steps manually:"
                )
            }
            Then("And finished alert callback should NOT be triggered") {
                verify {
                    isFinishedCallback.invoke(any()) wasNot Called
                }
            }
        }
        Scenario("When process finished") {
            lateinit var configurationId: String
            lateinit var givenStepConfigurations: List<StepConfigurationModel>
            lateinit var givenProcessConfiguration: ProcessConfiguration
            lateinit var givenStepOne: Step
            lateinit var givenStepTwo: Step
            lateinit var givenProcessInstanceId: String
            lateinit var givenProcessInstance: ProcessInstanceState

            Given("Configuration id") {
                configurationId = "configurationId"
            }
            Given("Step and process instance with stepOne and stepTwo") {
                givenProcessInstanceId = "givenProcessInstanceId"
                givenStepOne = `given step`(
                    configurationId = configurationId,
                    withProcessInstanceId = true,
                    processInstanceId = ProcessInstanceId(givenProcessInstanceId)
                )
                givenStepTwo = `given step`(
                    configurationId = configurationId,
                    withProcessInstanceId = true,
                    processInstanceId = ProcessInstanceId(givenProcessInstanceId),
                    isLast = true
                )
                givenProcessInstance = `given process instance state in FINISHED stage`(
                    processInstanceId = ProcessInstanceId(givenProcessInstanceId),
                    configurationId = configurationId,
                    withSteps = listOf(givenStepOne, givenStepTwo)
                )
            }
            Given("Process configuration with two steps") {
                givenStepConfigurations = listOf(
                    `given single step configuration`(
                        configurationId = configurationId,
                        stepId = givenStepOne.stepId.value,
                        isFirst = true
                    ),
                    `given single step configuration`(
                        configurationId,
                        stepId = givenStepTwo.stepId.value,
                        isLast = true
                    )
                )

                givenProcessConfiguration = `given process configuration`(Duration.ZERO, configurationId)
            }

            Given("ProcessConfigurationRepository return givenProcessConfiguration") {
                every { processConfigurationRepository.getBy(ProcessConfigurationId(configurationId)) } returns givenProcessConfiguration
            }
            Given("StepConfigurationRepository return givenStepConfigurations") {
                every { stepConfigurationRepository.getById(ProcessConfigurationId(configurationId)) } returns givenStepConfigurations
            }

            lateinit var isFinishedAlertedString: MutableList<String>
            Given("Is finished callback captured") {
                isFinishedAlertedString = mutableListOf()
                every { isFinishedCallback.invoke(capture(isFinishedAlertedString)) } just runs
            }

            When("Alert is triggered on givenProcessInstance") {
                alert.alertOn(listOf(givenProcessInstance))
            }

            Then("Not finished alert callback should be triggered with properly formatted string") {
                assertThat(isFinishedAlertedString).isNotEmpty
                assertThat(isFinishedAlertedString.first()).contains(
                    "Process finished but exceeded",
                    "expected time window",
                    "Please review steps manually:"
                )
            }
            Then("And not finished alert callback should NOT be triggered") {
                verify {
                    isNotFinishedCallback.invoke(any()) wasNot Called
                }
            }
        }
    }
})