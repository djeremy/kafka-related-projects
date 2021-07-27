package com.djeremy.process.monitor.domain.streams

import com.djeremy.avro.business.process.monitor.v2.Reference
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import com.djeremy.process.monitor.domain.streams.step.DefaultStepTransformer
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.apache.avro.generic.GenericRecord
import org.assertj.core.api.SoftAssertions.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import com.djeremy.process.monitor.domain.process.models.Reference as ModelReference

internal class DefaultStepTransformerTest : Spek({

    describe("Test Default Step Transformer transform method") {
        val stepConfigurationModel = mockk<StepConfigurationModel>()
        val transformer = DefaultStepTransformer(stepConfigurationModel)

        val eventId = "eventId"
        val isLast = true
        val configurationValue = "configurationValue"
        val stepValue = "stepValue"


        afterEachTest { clearMocks(stepConfigurationModel) }

        it("should return step when empty references list returned " +
                "and event id is set to eventId") {
            // given
            val key = "key"
            val event = mockk<GenericRecord>()

            every { stepConfigurationModel.getEventId(key, event) } returns eventId
            every { stepConfigurationModel.getConfigurationId().value } returns configurationValue
            every { stepConfigurationModel.getId().value } returns stepValue
            every { stepConfigurationModel.checkIfLast(event)} returns isLast
            every { stepConfigurationModel.getReferences(event) } returns emptyList()

            // when
            val result = transformer.transform(key, event)

            // then
            assertSoftly{

                it.assertThat(result.key).isEqualTo(configurationValue)

                it.assertThat(result.value.getConfigurationId()).isEqualTo(configurationValue)
                it.assertThat(result.value.getStepId()).isEqualTo(stepValue)
                it.assertThat(result.value.getIsLast()).isEqualTo(isLast)

                it.assertThat(result.value.getReferences())
                        .containsOnly(Reference(eventId, "eventId"))
            }
        }
        it("should return step when empty references list returned and correctly mapped") {
            // given
            val key = "key"
            val event = mockk<GenericRecord>()
            val referenceModel = ModelReference("referenceId", "referenceName")

            every { stepConfigurationModel.getEventId(key, event) } returns eventId
            every { stepConfigurationModel.getConfigurationId().value } returns configurationValue
            every { stepConfigurationModel.getId().value } returns stepValue
            every { stepConfigurationModel.checkIfLast(event)} returns isLast
            every { stepConfigurationModel.getReferences(event) } returns listOf(referenceModel)

            // when
            val result = transformer.transform(key, event)

            // then
            assertSoftly{

                it.assertThat(result.key).isEqualTo(configurationValue)

                it.assertThat(result.value.getConfigurationId()).isEqualTo(configurationValue)
                it.assertThat(result.value.getStepId()).isEqualTo(stepValue)
                it.assertThat(result.value.getIsLast()).isEqualTo(isLast)

                it.assertThat(result.value.getReferences())
                        .containsOnly(Reference(referenceModel.referenceId, referenceModel.referenceName))
            }
        }
    }
})