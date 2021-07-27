package com.djeremy.process.monitor.domain.process.models

import com.djeremy.process.monitor.domain.`given step`
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.UUID

internal class StepTest : Spek({

    Feature("Test Step model's methods") {

        Scenario("Test assign newInstanceId when success") {
            lateinit var step: Step
            var processInstanceId: ProcessInstanceId? = null
            Given("Step without process instance id") {
                step = `given step`()
                processInstanceId = ProcessInstanceId(UUID.randomUUID().toString())
            }
            When("Try to assign new processInstanceId") {
                step.assignNewInstanceId(processInstanceId!!)
            }
            Then("Step should have new instance and newly assigned flag set to true") {
                assertSoftly {
                    it.assertThat(step.isNewlyAssigned)
                            .isTrue
                    it.assertThat(step.processInstanceId)
                            .isEqualTo(processInstanceId)
                }
            }
        }

        Scenario("Test assign newInstanceId when fail") {
            lateinit var step: Step
            var processInstanceId: ProcessInstanceId? = null
            Given("Step with process instance id") {
                step = `given step`(withProcessInstanceId = true)
                processInstanceId = ProcessInstanceId(UUID.randomUUID().toString())
            }

            lateinit var resultThrowable: Throwable
            When("Try to assign new processInstanceId") {
                resultThrowable = catchThrowable { step.assignNewInstanceId(processInstanceId!!) }
            }
            Then("IllegalArgumentException should be thrown") {
                assertThat(resultThrowable)
                        .isInstanceOf(IllegalArgumentException::class.java)
                        .hasMessage("You cannot reassign processInstanceId as one already was assigned")
            }
        }

        Scenario("Test assign oldInstanceId when success") {
            lateinit var step: Step
            var processInstanceId: ProcessInstanceId? = null
            Given("Step without process instance id") {
                step = `given step`()
                processInstanceId = ProcessInstanceId(UUID.randomUUID().toString())
            }
            When("Try to assign old processInstanceId") {
                step.assignOldInstanceId(processInstanceId!!)
            }
            Then("Step should have new instance and newly assigned flag set to true") {
                assertSoftly {
                    it.assertThat(step.isNewlyAssigned)
                            .isFalse
                    it.assertThat(step.processInstanceId)
                            .isEqualTo(processInstanceId)
                }
            }
        }

        Scenario("Test assign oldInstanceId when fail") {
            lateinit var step: Step
            var processInstanceId: ProcessInstanceId? = null
            Given("Step with process instance id") {
                step = `given step`(withProcessInstanceId = true)
                processInstanceId = ProcessInstanceId(UUID.randomUUID().toString())
            }

            lateinit var resultThrowable: Throwable
            When("Try to assign old processInstanceId") {
                resultThrowable = catchThrowable { step.assignOldInstanceId(processInstanceId!!) }
            }
            Then("IllegalArgumentException should be thrown") {
                assertThat(resultThrowable)
                        .isInstanceOf(IllegalArgumentException::class.java)
                        .hasMessage("You cannot reassign processInstanceId as one already was assigned")
            }
        }

        Scenario("Test admit when success") {
            lateinit var step: Step
            Given("Step with process instance id") {
                step = `given step`(withProcessInstanceId = true)
            }
            When("Try to admit") {
                step.mature()
            }
            Then("Step should have newly assigned flag set to false") {
                assertThat(step.isNewlyAssigned)
                        .isFalse()
            }
        }

        Scenario("Test admit when fail") {
            lateinit var step: Step
            Given("Step without process instance id") {
                step = `given step`()
            }

            lateinit var resultThrowable: Throwable
            When("Try to admit") {
                resultThrowable = catchThrowable { step.mature() }
            }
            Then("Step should have newly assigned flag set to false") {
                assertThat(resultThrowable).isInstanceOf(IllegalArgumentException::class.java)
                        .hasMessage("You cannot admit not yet assigned step")
            }
        }
    }
})