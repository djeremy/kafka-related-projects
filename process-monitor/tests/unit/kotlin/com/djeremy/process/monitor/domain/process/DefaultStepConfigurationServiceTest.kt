package com.djeremy.process.monitor.domain.process

import com.djeremy.process.monitor.randomUUID
import com.djeremy.process.monitor.domain.`given single step configuration`
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
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

internal class DefaultStepConfigurationServiceTest : Spek({
    describe("test DefaultStepConfiguration Service") {
        val repository = mockk<StepConfigurationRepository>()
        val service = DefaultStepConfigurationService(repository)

        afterEachGroup { clearMocks(repository) }

        context("saveOrUpdate configuration in repository ") {
            it("when new steps received") {
                // given
                val processConfigurationId = ProcessConfigurationId(randomUUID().toString())
                val steps = listOf(`given single step configuration`(configurationId = processConfigurationId.value))

                every { repository.getById(processConfigurationId) } returns emptyList()
                every { repository.saveAll(any()) } just runs

                // when
                service.save(steps)

                // then
                verify { repository.saveAll(steps) }
            }
            it("when steps should be updated when different size of steps") {
                // given
                val processConfigurationId = ProcessConfigurationId(randomUUID().toString())
                val steps = listOf(
                        `given single step configuration`(configurationId = processConfigurationId.value),
                        `given single step configuration`(configurationId = processConfigurationId.value))
                val savedSteps = listOf(`given single step configuration`(configurationId = processConfigurationId.value))

                every { repository.getById(processConfigurationId) } returns savedSteps
                every { repository.delete(any()) } just runs
                every { repository.saveAll(any()) } just runs

                // when
                service.save(steps)

                // then
                verify {
                    repository.delete(savedSteps)
                    repository.saveAll(steps)
                }
            }
            it("when steps should be updated when steps are different") {
                // given
                val processConfigurationId = ProcessConfigurationId(randomUUID().toString())
                val steps = listOf(`given single step configuration`(configurationId = processConfigurationId.value))
                val savedSteps = listOf(`given single step configuration`())

                every { repository.getById(processConfigurationId) } returns savedSteps
                every { repository.delete(any()) } just runs
                every { repository.saveAll(any()) } just runs

                // when
                service.save(steps)

                // then
                verify {
                    repository.delete(savedSteps)
                    repository.saveAll(steps)
                }
            }
            it("when validation failed if steps with different configuration supplied") {
                // given
                val steps = listOf(`given single step configuration`(), `given single step configuration`())

                // when
                val result = catchThrowable { service.save(steps) }

                // then
                assertThat(result).isInstanceOf(IllegalArgumentException::class.java)
                        .hasMessageContaining("All steps should have only one configurationId")
            }
            it("when validation failed if steps are not unique") {
                // given
                val step = `given single step configuration`()
                val steps = listOf(step, step)

                // when
                val result = catchThrowable { service.save(steps) }

                // then
                assertThat(result).isInstanceOf(IllegalArgumentException::class.java)
                        .hasMessageContaining("Steps should be unique")
            }
        }
    }
})