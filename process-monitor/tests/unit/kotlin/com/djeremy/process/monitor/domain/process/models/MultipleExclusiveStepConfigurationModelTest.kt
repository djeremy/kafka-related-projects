package com.djeremy.process.monitor.domain.process.models

import io.mockk.every
import io.mockk.mockk
import org.apache.avro.generic.GenericRecord
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import scala.Tuple4

internal class MultipleExclusiveStepConfigurationModelTest : Spek({
    val id = StepId("id")
    val configurationId = ProcessConfigurationId("configurationId")
    val description = "description"
    val topic = "topic"
    val schemaName = "schemaName"
    val eventIdSchemaPath = "eventIdSchemaPath"
    val referenceIdSchemaPaths: List<String> = emptyList()
    val isFirst = true
    val isLast = false
    val alternativeSchemaName = "alternativeSchemaName"
    val alternativeEventIdSchemaPath = "alternativeEventIdSchemaPath"
    val alternativeReferenceIdsSchemaPaths: List<String> = emptyList()
    val alternativeIsLast = false

    describe("Test multipleExclusive step configuration model methods on given instance") {
        val model = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

        it("generateNewId returns new id") {
            // when
            val id1 = model.generateNewId()
            val id2 = model.generateNewId()
            // then
            assertThat(id1).isNotEqualTo(id2)
        }
        it("shouldAccept returns true when original") {
            // given
            val event = mockk<GenericRecord>()
            every { event.schema.fullName } returns schemaName
            // when
            val result = model.shouldAccept(event)
            // then
            assertThat(result).isTrue()
        }
        it("shouldAccept returns true when alternative") {
            // given
            val event = mockk<GenericRecord>()
            val sameSchemaName = alternativeSchemaName
            every { event.schema.fullName } returns sameSchemaName
            // when
            val result = model.shouldAccept(event)
            // then
            assertThat(result).isTrue()
        }
        it("shouldAccept returns false") {
            // given
            val event = mockk<GenericRecord>()
            val sameSchemaName = "notApplicableSchemaName"
            every { event.schema.fullName } returns sameSchemaName
            // when
            val result = model.shouldAccept(event)
            // then
            assertThat(result).isFalse()
        }
    }

    describe("Test equals method") {
        it("when true all fields match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            // when
            val result = step1 == step2
            // then
            assertThat(result).isTrue()
        }
        it("when true all fields match except Id") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(StepId("notMatched"), configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            // when
            val result = step1 == step2
            // then
            assertThat(result).isTrue()
        }
        it("when false configurationId not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, ProcessConfigurationId("notMatched"), description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false description not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, "not match", topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false topic not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, "not match",
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false schemaName not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    "not match", eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false eventIdSchemaPath not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, "not match", referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false referenceIdSchemaPaths not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, listOf("not match"), isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false alternativeSchemaName not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, "not match",
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false alternativeEventIdSchemaPath not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    "not match", alternativeReferenceIdsSchemaPaths, alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false alternativeReferenceIdsSchemaPaths not match") {
            // given
            val step1 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, alternativeIsLast)
            val step2 = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast, alternativeSchemaName,
                    alternativeEventIdSchemaPath, listOf("not match"), alternativeIsLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
    }

    describe("Test checkIfLast with different conditions") {
        context("with params") {
            val stepAlternativeLast = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, false, false, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, true)
            val stepLast = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, false, true, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, false)
            val stepBothLast = MultipleExclusiveStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, false, true, alternativeSchemaName,
                    alternativeEventIdSchemaPath, alternativeReferenceIdsSchemaPaths, true)

            val eventWithAlternativeSchema: GenericRecord = mockk { every { schema.fullName } returns alternativeSchemaName }
            val eventWithSchema: GenericRecord = mockk { every { schema.fullName } returns schemaName }

            val params = listOf(
                    Tuple4("alternative when isLast is true", stepAlternativeLast, {eventWithAlternativeSchema}, true),
                    Tuple4("alternative when isLast is false", stepAlternativeLast, {eventWithSchema}, false),
                    Tuple4("schema when isLast is false", stepLast, {eventWithAlternativeSchema}, false),
                    Tuple4("schema when isLast is true", stepLast, {eventWithSchema}, true),
                    Tuple4("both(Alternative) when isLast is true", stepBothLast, {eventWithAlternativeSchema}, true),
                    Tuple4("both(Primary) when isLast is true", stepBothLast, {eventWithSchema}, true)
            )

            params.forEach {
                it(it._1()) {
                    // given
                    every { eventWithAlternativeSchema.schema.fullName}  returns alternativeSchemaName
                    every { eventWithSchema.schema.fullName}  returns schemaName

                    val step = it._2()
                    val event = it._3()
                    val expected = it._4()

                    // when
                    val checkIfLast = step.checkIfLast(event())

                    // then
                    assertThat(checkIfLast).isEqualTo(expected)
                }
            }
        }
    }
})