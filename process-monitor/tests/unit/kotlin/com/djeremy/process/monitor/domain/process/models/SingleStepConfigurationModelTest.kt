package com.djeremy.process.monitor.domain.process.models

import io.mockk.every
import io.mockk.mockk
import org.apache.avro.generic.GenericRecord
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class SingleStepConfigurationModelTest : Spek({
    // given shared step data
    val id = StepId("id")
    val configurationId = ProcessConfigurationId("configurationId")
    val description = "description"
    val topic = "topic"
    val schemaName = "schemaName"
    val eventIdSchemaPath = "eventIdSchemaPath"
    val referenceIdSchemaPaths: List<String> = emptyList()
    val isFirst = true
    val isLast = false

    describe("Test single step configuration model methods on given instance") {

        val model = SingleStepConfigurationModel(id, configurationId, description, topic,
                schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

        it("generateNewId returns new id") {
            // when
            val id1 = model.generateNewId()
            val id2 = model.generateNewId()
            // then
            assertThat(id1).isNotEqualTo(id2)
        }
        it("shouldAccept returns true") {
            // given
            val event = mockk<GenericRecord>()
            every { event.schema.fullName } returns schemaName
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

    describe("test equals method") {
        it("when true all fields match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

            // when
            val result = step1 == step2
            // then
            assertThat(result).isTrue()
        }
        it("when true all fields match except Id") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(StepId("notMatched"), configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

            // when
            val result = step1 == step2
            // then
            assertThat(result).isTrue()
        }
        it("when false configurationId not match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, ProcessConfigurationId("notMatched"),
                    description, topic, schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false description not match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, configurationId, "not match", topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false topic not match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, configurationId, description, "not match",
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false schemaName not match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    "not match", eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false eventIdSchemaPath not match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, "not match", referenceIdSchemaPaths, isFirst, isLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
        it("when false referenceIdSchemaPaths not match") {
            // given
            val step1 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, referenceIdSchemaPaths, isFirst, isLast)
            val step2 = SingleStepConfigurationModel(id, configurationId, description, topic,
                    schemaName, eventIdSchemaPath, listOf("not match"), isFirst, isLast)

            val result = step1 == step2
            // then
            assertThat(result).isFalse()
        }
    }
})