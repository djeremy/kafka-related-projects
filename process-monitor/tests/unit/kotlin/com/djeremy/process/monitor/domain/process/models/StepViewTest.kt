package com.djeremy.process.monitor.domain.process.models

import com.djeremy.process.monitor.domain.`given single step configuration`
import com.djeremy.process.monitor.domain.`given step`
import com.djeremy.process.monitor.domain.toView
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.format.DateTimeFormatter

internal class StepViewTest : Spek({
    describe("test StepView formatAccordingTo method") {

        it("should format correctly") {
            // Given
            val configurationId = "configurationId"
            val stepView = `given step`(
                configurationId = configurationId
            ).toView()
            val stepConfigurationModel = `given single step configuration`(
                configurationId = configurationId,
                stepId = stepView.stepId.value,
                isFirst = true
            )

            // When
            val result = stepView.formatAccordingTo(stepConfigurationModel)

            // Then
            assertThat(result).isEqualTo(
                """
                    |Step[
                    |    eventId=${stepView.eventId}"
                    |    description="${stepConfigurationModel.getDescription()}"
                    |    receivedAt="${stepView.receivedAt.format(DateTimeFormatter.ISO_DATE_TIME)}
                    |    references=${
                    stepView.references.joinToString("], [", "[", "]") {
                        "refId=\"${it.referenceId}\", refName=\"${it.referenceName}\""
                    }
                }
                    |]
                """.trimMargin("|")
            )
        }
    }
})