package com.djeremy.process.monitor.domain.process

import com.djeremy.avro.test.v2.Command
import com.djeremy.avro.test.v2.NestedCommand
import com.djeremy.process.monitor.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import scala.Tuple3

internal class GenericRecordExtractorKtTest : Spek({

    describe("Test testExtractFieldStringFrom with different conditions") {

        context("testExtractFieldStringFrom from Command and different paths") {
            val commandId = randomUUID()
            val event1Id = randomUUID()
            val event2Id = randomUUID()
            val command = Command(commandId, event1Id, event2Id)

            val params = listOf(
                    Tuple3(command, "id.value", commandId.getValue()),
                    Tuple3(command, "event1Id.value", event1Id.getValue()),
                    Tuple3(command, "event2Id.value", event2Id.getValue())
            )

            params.forEach {
                it("when ${it._2()} should return ${it._3()}") {
                    // when
                    val result = extractFieldStringFrom(it._2(), command)
                    // then
                    assertThat(result).isEqualTo(it._3())
                }
            }
        }

        context("testExtractFieldStringFrom from NestedCommand and different paths (without .value prefix)") {
            val commandId = randomUUID()
            val event1Id = randomUUID()
            val event2Id = randomUUID()
            val command = Command(commandId, event1Id, event2Id)
            val nestedCommandId = randomUUID()
            val otherId = randomUUID()
            val nestedCommand = NestedCommand(nestedCommandId, command, otherId)

            val params = listOf(
                    Tuple3(nestedCommand, "id", nestedCommandId.getValue()),
                    Tuple3(nestedCommand, "id.value", nestedCommandId.getValue()),
                    Tuple3(nestedCommand, "nestedCommand.id", commandId.getValue()),
                    Tuple3(nestedCommand, "nestedCommand.event1Id", event1Id.getValue()),
                    Tuple3(nestedCommand, "nestedCommand.event1Id.value", event1Id.getValue()),
                    Tuple3(nestedCommand, "nestedCommand.event2Id", event2Id.getValue()),
                    Tuple3(nestedCommand, "otherId", otherId.getValue()),
                    Tuple3(nestedCommand, "otherId.value", otherId.getValue()),
                    Tuple3(nestedCommand, "otherId.value.wrongPath", null),
                    Tuple3(nestedCommand, "wrongPath", null),
                    Tuple3(nestedCommand, "nestedCommand.wrongPath", null)
            )

            params.forEach {
                it("when ${it._2()} should return ${it._3()}") {
                    // when
                    val result = extractFieldStringFrom(it._2(), it._1())
                    // then
                    assertThat(result).isEqualTo(it._3())
                }
            }
        }

        context("testExtractFieldStringFrom(nullable) from Command") {
            val commandId = randomUUID()
            val event1Id = randomUUID()
            val event2Id = randomUUID()
            val command = Command(commandId, event1Id, event2Id)
            val key = "key"

            it("when schema path is null and key returned instead") {
                // when
                val result = extractFieldStringFrom(null, key, command)
                // then
                assertThat(result).isEqualTo(key)
            }
        }
    }
})