package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.`given process configuration`
import com.djeremy.process.monitor.domain.`given process instance state in FINISHED stage`
import com.djeremy.process.monitor.domain.`given process instance state in NEW stage`
import com.djeremy.process.monitor.domain.`given step from`
import com.djeremy.process.monitor.domain.`given step`
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStateProjection
import com.djeremy.process.monitor.utils.f
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.time.Duration
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds
import java.time.LocalDateTime.now
import kotlin.streams.toList

internal class DefaultProcessInstanceExpiredTaskTest : Spek({

    fun ProcessInstanceState.toProjection(): ProcessInstanceStateProjection = ProcessInstanceStateProjection(instance)

    Feature("test execute task with different scenarios") {

        val processInstanceStateRepository = mockk<ProcessInstanceStateRepository>()
        val processConfigurationRepository = mockk<ProcessConfigurationRepository>()
        val alert = mockk<ProcessExpiredAlert>()

        val lagDuration = ofMinutes(1)
        val pageSize = 1000

        val task: ProcessInstanceExpiredTask = DefaultProcessInstanceExpiredTask(
                processInstanceStateRepository, processConfigurationRepository,
                alert, lagDuration, pageSize
        )
        afterEachScenario { clearMocks(processConfigurationRepository, processInstanceStateRepository, alert) }

        Scenario("when process should be admitted if configuration not found") {

            lateinit var processInstance: ProcessInstanceState
            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("process instance in NEW state and not existing configuration") {
                processInstance = `given process instance state in NEW stage`()
                every { processInstanceStateRepository.getNotAdmittedIdsBefore(any()) } returns listOf(processInstance.toProjection())
                every { processInstanceStateRepository.getBy(listOf(processInstance.instance)) } returns listOf(processInstance)
                every { processConfigurationRepository.getBy(processInstance.instance.configurationId) } returns null

                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs

                every { alert.alertOn(capture(alertedSteps)) } just runs

            }
            When("execute method is called") {
                task.execute()
            }

            Then("process instance should be admitted") {
                assertThat(admittedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f(), ProcessInstanceState::isAdmitted.f())
                        .contains(tuple(processInstance.instance, true))
            }
            Then("process instance should NOT be alerted") {
                assertThat(alertedSteps.captured)
                        .hasSize(0)
            }
        }

        Scenario("when process should be admitted if finished and not expired") {

            lateinit var processInstance: ProcessInstanceState
            lateinit var configuration: ProcessConfiguration
            lateinit var duration: Duration

            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("process instance in FINISHEd state " +
                    "and last step came before expected time and expected time is less the current data") {
                duration = ofSeconds(60)
                configuration = `given process configuration`(duration)
                val firstStep = `given step`(configurationId = configuration.id.value,
                        withProcessInstanceId = true,
                        receivedAt = now().minus(ofSeconds(70)))
                val lastStep = `given step from`(firstStep, isLast = true,
                        receivedAt = now().minus(ofSeconds(50)))

                processInstance = `given process instance state in FINISHED stage`(withSteps = listOf(firstStep, lastStep))

            }
            Given("mocks with responses"){
                every { processInstanceStateRepository.getNotAdmittedIdsBefore(any()) } returns listOf(processInstance.toProjection())
                every { processInstanceStateRepository.getBy(listOf(processInstance.instance)) } returns listOf(processInstance)
                every { processConfigurationRepository.getBy(processInstance.instance.configurationId) } returns configuration

                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs

                every { alert.alertOn(capture(alertedSteps)) } just runs
            }

            When("execute method is called") {
                task.execute()
            }

            Then("process instance should be admitted") {
                assertThat(admittedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f(), ProcessInstanceState::isAdmitted.f())
                        .contains(tuple(processInstance.instance, true))
            }
            Then("process instance should NOT be alerted") {
                assertThat(alertedSteps.captured)
                        .hasSize(0)
            }
        }

        Scenario("when process should be admitted if finished (upfront) and not expired") {

            lateinit var processInstance: ProcessInstanceState
            lateinit var configuration: ProcessConfiguration
            lateinit var expectedToFinishIn: Duration

            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("process instance in FINISHEd state " +
                    "and last step came before expected time and expected time is less the current data") {
                expectedToFinishIn = ofSeconds(60)
                configuration = `given process configuration`(expectedToFinishIn)
                val firstStep = `given step`(configurationId = configuration.id.value,
                        withProcessInstanceId = true,
                        receivedAt = now().minus(ofSeconds(15)))
                val lastStep = `given step from`(firstStep, isLast = true,
                        receivedAt = now().minus(ofSeconds(10)))

                processInstance = `given process instance state in FINISHED stage`(withSteps = listOf(firstStep, lastStep))

            }
            Given("mocks with responses"){
                every { processInstanceStateRepository.getNotAdmittedIdsBefore(any()) } returns listOf(processInstance.toProjection())
                every { processInstanceStateRepository.getBy(listOf(processInstance.instance)) } returns listOf(processInstance)
                every { processConfigurationRepository.getBy(processInstance.instance.configurationId) } returns configuration

                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs

                every { alert.alertOn(capture(alertedSteps)) } just runs
            }

            When("execute method is called") {
                task.execute()
            }

            Then("process instance should be admitted") {
                assertThat(admittedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f(), ProcessInstanceState::isAdmitted.f())
                        .contains(tuple(processInstance.instance, true))
            }
            Then("process instance should NOT be alerted") {
                assertThat(alertedSteps.captured)
                        .hasSize(0)
            }
        }

        Scenario("when process should be ignored if NOT finished and did not exceed expected time") {

            lateinit var processInstance: ProcessInstanceState
            lateinit var configuration: ProcessConfiguration
            lateinit var expectedToFinishIn: Duration

            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("process instance in NEW state " +
                    "and first step come just now but not expired yet") {
                expectedToFinishIn = ofSeconds(60)
                configuration = `given process configuration`(expectedToFinishIn)
                val firstStep = `given step`(configurationId = configuration.id.value,
                        withProcessInstanceId = true,
                        receivedAt = now())

                processInstance = `given process instance state in NEW stage`(withSteps = listOf(firstStep))

            }
            Given("mocks with responses"){
                every { processInstanceStateRepository.getNotAdmittedIdsBefore(any()) } returns listOf(processInstance.toProjection())
                every { processInstanceStateRepository.getBy(listOf(processInstance.instance)) } returns listOf(processInstance)
                every { processConfigurationRepository.getBy(processInstance.instance.configurationId) } returns configuration

                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs

                every { alert.alertOn(capture(alertedSteps)) } just runs
            }

            When("execute method is called") {
                task.execute()
            }

            Then("process instance should NOT be admitted (ignored for next execution)") {
                assertThat(admittedSteps.captured)
                        .hasSize(0)
            }
            Then("process instance should NOT be alerted") {
                assertThat(alertedSteps.captured)
                        .hasSize(0)
            }
        }

        Scenario("when process should be alerted if finished and expired") {

            lateinit var processInstance: ProcessInstanceState
            lateinit var configuration: ProcessConfiguration
            lateinit var expectedToFinishIn: Duration

            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("process instance in FINISHEd state " +
                    "and last step came after expected time") {
                expectedToFinishIn = ofSeconds(60)
                configuration = `given process configuration`(expectedToFinishIn)
                val firstStep = `given step`(configurationId = configuration.id.value,
                        withProcessInstanceId = true,
                        receivedAt = now().minus(ofSeconds(80)))
                val lastStep = `given step from`(firstStep, isLast = true,
                        receivedAt = now().minus(ofSeconds(10)))

                processInstance = `given process instance state in FINISHED stage`(withSteps = listOf(firstStep, lastStep))

            }
            Given("mocks with responses"){
                every { processInstanceStateRepository.getNotAdmittedIdsBefore(any()) } returns listOf(processInstance.toProjection())
                every { processInstanceStateRepository.getBy(listOf(processInstance.instance)) } returns listOf(processInstance)
                every { processConfigurationRepository.getBy(processInstance.instance.configurationId) } returns configuration

                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs

                every { alert.alertOn(capture(alertedSteps)) } just runs
            }

            When("execute method is called") {
                task.execute()
            }

            Then("process instance should be admitted") {
                assertThat(admittedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f(), ProcessInstanceState::isAdmitted.f())
                        .contains(tuple(processInstance.instance, true))
            }
            Then("process instance should be alerted") {
                assertThat(alertedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f())
                        .containsOnly(processInstance.instance)
            }
        }

        Scenario("when process should be alerted if NOT finished but expired") {

            lateinit var processInstance: ProcessInstanceState
            lateinit var configuration: ProcessConfiguration
            lateinit var expectedToFinishIn: Duration

            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("process instance in NEW state " +
                    "and last state didn't come in expected time window") {
                expectedToFinishIn = ofSeconds(60)
                configuration = `given process configuration`(expectedToFinishIn)
                val firstStep = `given step`(configurationId = configuration.id.value,
                        withProcessInstanceId = true,
                        receivedAt = now().minus(ofSeconds(80)))

                processInstance = `given process instance state in NEW stage`(withSteps = listOf(firstStep))
            }
            Given("mocks with responses"){
                every { processInstanceStateRepository.getNotAdmittedIdsBefore(any()) } returns listOf(processInstance.toProjection())
                every { processInstanceStateRepository.getBy(listOf(processInstance.instance)) } returns listOf(processInstance)
                every { processConfigurationRepository.getBy(processInstance.instance.configurationId) } returns configuration

                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs

                every { alert.alertOn(capture(alertedSteps)) } just runs
            }

            When("execute method is called") {
                task.execute()
            }

            Then("process instance should be admitted") {
                assertThat(admittedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f(), ProcessInstanceState::isAdmitted.f())
                        .contains(tuple(processInstance.instance, true))
            }
            Then("process instance should be alerted") {
                assertThat(alertedSteps.captured)
                        .hasSize(1)
                        .extracting(ProcessInstanceState::instance.f())
                        .containsOnly(processInstance.instance)
            }
        }
    }

    Feature("test admitAndAlert function") {
        val processInstanceStateRepository = mockk<ProcessInstanceStateRepository>()
        val processConfigurationRepository = mockk<ProcessConfigurationRepository>()
        val alert = mockk<ProcessExpiredAlert>()

        val lagDuration = ofMinutes(1)
        val pageSize = 1000

        val task = DefaultProcessInstanceExpiredTask(
            processInstanceStateRepository, processConfigurationRepository,
            alert, lagDuration, pageSize
        )
        afterEachScenario { clearMocks(processConfigurationRepository, processInstanceStateRepository, alert) }


        Scenario("when process should be alerted twice") {
            lateinit var processInstance: List<ProcessInstanceState>
            lateinit var configuration: ProcessConfiguration
            lateinit var configuration2: ProcessConfiguration
            lateinit var expectedToFinishIn: Duration
            lateinit var configurationCache: MutableMap<ProcessConfigurationId, ProcessConfiguration?>
            val admittedSteps = slot<List<ProcessInstanceState>>()
            val alertedSteps = slot<List<ProcessInstanceState>>()

            Given("both process instance in NEW state " +
                    "and last state didn't come in expected time window") {
                expectedToFinishIn = ofSeconds(60)
                configuration = `given process configuration`(expectedToFinishIn)
                configuration2 = `given process configuration`(expectedToFinishIn)
                val firstStepReceivedAt = ofSeconds(80)
                val lastStepReceivedAt = ofSeconds(10)
                val firstStep = `given step`(configurationId = configuration.id.value,
                    withProcessInstanceId = true,
                    receivedAt = now().minus(firstStepReceivedAt))
                val lastStep = `given step from`(firstStep, isLast = true,
                    receivedAt = now().minus(lastStepReceivedAt))

                val firstStep2 = `given step`(configurationId = configuration2.id.value,
                    withProcessInstanceId = true,
                    receivedAt = now().minus(firstStepReceivedAt))
                val lastStep2 = `given step from`(firstStep, isLast = true,
                    receivedAt = now().minus(lastStepReceivedAt))

                processInstance = listOf(
                    `given process instance state in FINISHED stage`(withSteps = listOf(firstStep, lastStep)),
                    `given process instance state in FINISHED stage`(withSteps = listOf(firstStep2, lastStep2))
                )
                configurationCache = mutableMapOf(
                    processInstance[0].instance.configurationId to configuration,
                    processInstance[1].instance.configurationId to configuration2
                )
            }
            Given("mocks with responses"){
                every { processInstanceStateRepository.getBy(processInstance.stream().map { it.instance }.toList()) } returns processInstance
                every { processConfigurationRepository.getBy(processInstance[0].instance.configurationId) } returns configuration
                every { processConfigurationRepository.getBy(processInstance[1].instance.configurationId) } returns configuration2

                every { alert.alertOn(capture(alertedSteps)) } just runs
                every { processInstanceStateRepository.saveAll(capture(admittedSteps)) } just runs
            }

            When("execute method is called") {
                task.admitAndAlert(processInstance.stream().map { ProcessInstanceStateProjection(it.instance) }.toList(), configurationCache)
            }

            Then("process instance should be admitted") {
                assertThat(admittedSteps.captured)
                    .hasSize(2)
            }
            Then("process instance should be alerted") {
                assertThat(alertedSteps.captured)
                    .hasSize(2)
            }
            Then("admit shoud be executed once") {
                verify(exactly = 1)  { processInstanceStateRepository.saveAll(any()) }
            }
            Then("alerts should be executed once") {
                verify(exactly = 1)  { alert.alertOn(any()) }
            }
        }
    }
})