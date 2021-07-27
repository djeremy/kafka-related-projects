package com.djeremy.process.monitor.adapter.properties

import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class MappersKtTest : Spek({

    describe("Test ProcessConfigurationPropertiesV2.toModelWithSteps") {
        it("should set only one first step") {
            // given
            val properties = `given random properties with given number of steps`(5)

            // when
            val result = properties.toModelWithSteps()

            // then
            val onlyFirstSteps = result.steps.filter { it.isFirst() }

            assertThat(onlyFirstSteps)
                    .hasSize(1)
            val actualFirstStep = onlyFirstSteps.first()
            assertThat(actualFirstStep.getConfigurationId() to actualFirstStep.getTopic())
                    .isEqualTo(ProcessConfigurationId(properties.id!!) to properties.convertedSteps.first().topic)
        }
        it("should generate unique step ids on each call but same process configuration model") {
            // given
            val properties = `given random properties with given number of steps`(5)

            // when
            val result1 = properties.toModelWithSteps()
            val result2 = properties.toModelWithSteps()

            // then
            val result1StepIds = result1.steps.map { it.getId() }
            val result2StepIds = result2.steps.map { it.getId() }

            assertThat(result1StepIds)
                    .doesNotContainAnyElementsOf(result2StepIds)
            assertThat(result1.process)
                    .isEqualTo(result2.process)
        }
    }
})