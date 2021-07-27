package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.randomUUID
import com.djeremy.process.monitor.domain.`given multiple exclusive step configuration`
import com.djeremy.process.monitor.domain.`given single step configuration`
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationWithSteps
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.Duration

internal class DefaultProcessConfigurationServiceTest : Spek({
    describe("test DefaultProcessConfiguration Service ") {
        val processRepository = mockk<ProcessConfigurationRepository>()
        val stepConfigurationService = mockk<StepConfigurationService>()
        val service = DefaultProcessConfigurationService(processRepository, stepConfigurationService)

        afterEachGroup { clearMocks(processRepository, stepConfigurationService) }

        context("save configuration in repository") {
            it("for new business process") {
                // given
                val id = ProcessConfigurationId(randomUUID().toString())
                val businessProcess = ProcessConfiguration(
                        id = id,
                        description = randomUUID().getValue(),
                        duration = Duration.ofMinutes(1))
                val steps = listOf(
                        `given single step configuration`(id.value),
                        `given multiple exclusive step configuration`(id.value, isLast = true)
                )
                val model = ProcessConfigurationWithSteps(businessProcess, steps)

                every { processRepository.save(model.process) } just runs
                every { processRepository.getBy(model.process.id) } returns null
                every { stepConfigurationService.save(model.steps) } just runs

                // when
                service.save(model)

                // then
                verify(exactly = 1) { processRepository.save(model.process) }
                verify(exactly = 1) { stepConfigurationService.save(model.steps) }
            }
            it("for existing business process") {
                // given
                val id = ProcessConfigurationId(randomUUID().toString())
                val businessProcess = ProcessConfiguration(
                        id = id,
                        description = randomUUID().getValue(),
                        duration = Duration.ofMinutes(1))
                val steps = listOf(
                        `given single step configuration`(id.value),
                        `given multiple exclusive step configuration`(id.value, isLast = true)
                )
                val model = ProcessConfigurationWithSteps(businessProcess, steps)

                every { processRepository.getBy(model.process.id) } returns model.process
                every { processRepository.getBy(model.process.id) } returns null
                every { stepConfigurationService.getBy(model.process.id) } returns model.steps

                every { processRepository.save(model.process) } just runs
                every { stepConfigurationService.save(model.steps) } just runs

                // when
                service.save(model)

                // then
                verify(exactly = 1) { processRepository.save(model.process) }
                verify(exactly = 1) { stepConfigurationService.save(model.steps) }
            }
            it("validation should throw exception when is no finish condition") {
                // given
                val id = ProcessConfigurationId(randomUUID().toString())
                val businessProcess = ProcessConfiguration(
                        id = id,
                        description = randomUUID().getValue(),
                        duration = Duration.ofMinutes(1))
                val steps = listOf(
                        `given single step configuration`(id.value),
                        `given multiple exclusive step configuration`(id.value)
                )
                val model = ProcessConfigurationWithSteps(businessProcess, steps)

                // when

                val result = catchThrowable { service.save(model) }

                // then
                assertThat(result)
                        .isInstanceOf(IllegalArgumentException::class.java)
                        .hasMessageContainingAll("Process", id.value, " should have at least one last step")
            }
        }
    }
})