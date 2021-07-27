package com.djeremy.process.monitor.adapter.properties

import com.djeremy.process.monitor.domain.fakeValueService
import com.djeremy.process.monitor.domain.faker
import java.time.Duration

fun `given random properties with given number of steps`(@Suppress("SameParameterValue") numberOfSteps: Int): ProcessConfigurationPropertiesV2 =
        ProcessConfigurationPropertiesV2().apply {
            id = faker.bothify("??????????????")
            description = "description"
            steps = emptyList()
            expectToFinishIn = Duration.ofSeconds(10)

            val mapped: MutableList<StepConfigurationPropertiesV2> = (1..numberOfSteps).map { `given random step configuration properties`() }.toMutableList()
            mapped.removeAt(numberOfSteps - 1)
            mapped.add(`given random step configuration properties`(true))
            convertedSteps = mapped
        }

fun `given random step configuration properties`(isLast: Boolean = false): SingleStepConfiguration = SingleStepConfiguration().apply {
    type = "single"
    description = "description"
    schemaName = fakeValueService.bothify("wordldjeremy.something.#######")
    topic = "topic" + faker.bothify("?????")
    eventIdSchemaPath = "id.value"
    indicateProcessFinished = isLast
}