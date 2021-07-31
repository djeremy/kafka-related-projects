package com.djeremy.process.monitor.adapter.streams

import io.mockk.mockk
import org.apache.avro.specific.SpecificRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


internal class MappersKtTest : Spek({

    describe("Test resolve resolveStepModel") {

        it("should correctly map to Step when new processStep is received") {
            // given
            val processStep = `random ProcessStep`()

            // when
            val result = processStep.resolveStepModel()

            // then
            assertThat(result).isNotNull

            assertThat(result!!.configurationId.value).isEqualTo(processStep.getConfigurationId())
            assertSoftly { it ->
                it.assertThat(result.stepId.value).isEqualTo(processStep.getStepId())
                it.assertThat(result.eventId).isEqualTo(processStep.getEventId())
                it.assertThat(result.isLast).isEqualTo(processStep.getIsLast())

                it.assertThat(result.references.map { it.referenceId })
                        .containsOnly(*processStep.getReferences().map { it.getId() }.toTypedArray())
            }
        }
        it("should return null when wrong Specific record is supplied") {
            // given
            val processStep: SpecificRecord = mockk<SpecificRecord>()

            // when
            val result = processStep.resolveStepModel()

            // then
            assertThat(result).isNull()
        }
    }
})