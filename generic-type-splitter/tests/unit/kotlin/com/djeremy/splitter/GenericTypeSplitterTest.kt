package com.djeremy.splitter

import com.djeremy.avro.splitter.SplitterTypeOne
import com.djeremy.avro.splitter.SplitterTypeThree
import com.djeremy.avro.splitter.SplitterTypeTwo
import com.djeremy.splitter.GenericTypeSplitter.Companion.build
import com.djeremy.splitter.GenericTypeSplitter.SpecificNode.Companion.fromSchema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature


class GenericTypeSplitterTest : Spek({

    val inputTopic by memoized { "input.topic" }
    val outputTopics by memoized { listOf("output.one.topic", "output.two.topic", "output.three.topic") }

    val recordFactory by memoized(CachingMode.SCOPE) {
        RecordFactory(listOf(SplitterTypeOne(), SplitterTypeTwo(), SplitterTypeThree()))
    }

    val newValue by memoized { "new value" }
    val splitterTestInstance by memoized(CachingMode.TEST) {
        build {
            branchNext(fromSchema(SplitterTypeOne()))
            branchNext(fromSchema(SplitterTypeTwo()) {
                mapValues { value -> SplitterTypeThree(value.getId(), newValue) }
            })
            branchNext(fromSchema(SplitterTypeThree()) {
                mapValues { value -> value.setPropertyThree(newValue); value }
            })
        }
    }

    fun configureDriver(
        inputTopic: String,
        genericTypeSplitter: GenericTypeSplitter,
        outputTopics: List<String>
    ): TopologyTestDriver =
        with(StreamsBuilder()) {
            val stream = stream(
                inputTopic,
                Consumed.with(Serdes.String(), recordFactory.commonGenericSerde)
            )

            genericTypeSplitter.executeSplit(stream).zip(outputTopics).forEach {
                it.first.to(it.second, Produced.with(Serdes.String(), recordFactory.commonGenericSerde))
            }

            TopologyTestDriver(this.build(), properties(this::class))
        }


    Feature("Test splitter with multiple type configuration ") {

        Scenario("SplitterTypeOne supplied and has to land on output.one topic") {
            lateinit var testInputTopic: TestInputTopic<String, GenericRecord>
            lateinit var testOutputOneTopic: TestOutputTopic<String, SplitterTypeOne>
            lateinit var testOutputTwoTopic: TestOutputTopic<String, SplitterTypeThree>
            lateinit var testOutputThreeTopic: TestOutputTopic<String, SplitterTypeThree>

            lateinit var driver: TopologyTestDriver

            Given("Driver is configured with specific splitter") {
                driver = configureDriver(inputTopic, splitterTestInstance, outputTopics)
            }

            Given("Input and output TestTopics created") {
                testInputTopic = recordFactory.createGenericInputTopic(driver, inputTopic)
                testOutputOneTopic = recordFactory.createOutputTopic(driver, outputTopics[0])
                testOutputTwoTopic = recordFactory.createOutputTopic(driver, outputTopics[1])
                testOutputThreeTopic = recordFactory.createOutputTopic(driver, outputTopics[2])
            }

            lateinit var event: SplitterTypeOne
            lateinit var key: String
            Given("SplitterTypeOne event is created") {
                event = SplitterTypeOne("testMe")
                key = "key"
            }

            When("SplitterTypeOne event sent") {
                testInputTopic.pipeInput(key, event)
            }

            Then("Specific output topic received event and other are empty") {
                assertThat(testOutputOneTopic.readKeyValue())
                    .extracting("value").isEqualTo(event)
                assertThat(testOutputTwoTopic.isEmpty).isTrue
                assertThat(testOutputThreeTopic.isEmpty).isTrue
            }
        }

        Scenario("SplitterTypeTwo supplied and has to perform mapping and land on output.two topic") {
            lateinit var testInputTopic: TestInputTopic<String, GenericRecord>
            lateinit var testOutputOneTopic: TestOutputTopic<String, SplitterTypeOne>
            lateinit var testOutputTwoTopic: TestOutputTopic<String, SplitterTypeThree>
            lateinit var testOutputThreeTopic: TestOutputTopic<String, SplitterTypeThree>

            lateinit var driver: TopologyTestDriver

            Given("Driver is configured with specific splitter") {
                driver = configureDriver(inputTopic, splitterTestInstance, outputTopics)
            }

            Given("Input and output TestTopics created") {
                testInputTopic = recordFactory.createGenericInputTopic(driver, inputTopic)
                testOutputOneTopic = recordFactory.createOutputTopic(driver, outputTopics[0])
                testOutputTwoTopic = recordFactory.createOutputTopic(driver, outputTopics[1])
                testOutputThreeTopic = recordFactory.createOutputTopic(driver, outputTopics[2])
            }

            lateinit var event: SplitterTypeTwo
            lateinit var key: String
            Given("SplitterTypeTwo event is created") {
                event = SplitterTypeTwo("testMe", "someProperty")
                key = "key"
            }

            When("SplitterTypeTwo event sent") {
                testInputTopic.pipeInput(key, event)
            }

            Then("Specific output topic received event and other are empty") {
                assertThat(testOutputTwoTopic.readKeyValue())
                    .extracting("value").isEqualTo(
                        SplitterTypeThree(event.getId(), newValue)
                    )
                assertThat(testOutputOneTopic.isEmpty).isTrue
                assertThat(testOutputThreeTopic.isEmpty).isTrue
            }
        }

        Scenario("SplitterTypeThree supplied and has to perform mapping and land on output.three topic") {
            lateinit var testInputTopic: TestInputTopic<String, GenericRecord>
            lateinit var testOutputOneTopic: TestOutputTopic<String, SplitterTypeOne>
            lateinit var testOutputTwoTopic: TestOutputTopic<String, SplitterTypeThree>
            lateinit var testOutputThreeTopic: TestOutputTopic<String, SplitterTypeThree>

            lateinit var driver: TopologyTestDriver

            Given("Driver is configured with specific splitter") {
                driver = configureDriver(inputTopic, splitterTestInstance, outputTopics)
            }

            Given("Input and output TestTopics created") {
                testInputTopic = recordFactory.createGenericInputTopic(driver, inputTopic)
                testOutputOneTopic = recordFactory.createOutputTopic(driver, outputTopics[0])
                testOutputTwoTopic = recordFactory.createOutputTopic(driver, outputTopics[1])
                testOutputThreeTopic = recordFactory.createOutputTopic(driver, outputTopics[2])
            }

            lateinit var event: SplitterTypeThree
            lateinit var key: String
            Given("SplitterTypeThree event is created") {
                event = SplitterTypeThree("testMe", "someProperty")
                key = "key"
            }

            When("SplitterTypeThree event sent") {
                testInputTopic.pipeInput(key, event)
            }

            Then("Specific output topic received event and other are empty") {
                assertThat(testOutputThreeTopic.readKeyValue())
                    .extracting("value").isEqualTo(
                        SplitterTypeThree(event.getId(), newValue)
                    )
                assertThat(testOutputOneTopic.isEmpty).isTrue
                assertThat(testOutputTwoTopic.isEmpty).isTrue
            }
        }

    }
})