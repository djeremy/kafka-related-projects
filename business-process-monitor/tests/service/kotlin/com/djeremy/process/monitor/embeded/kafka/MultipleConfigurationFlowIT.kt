package com.djeremy.process.monitor.embeded.kafka

import com.djeremy.kafka.spring.test.KafkaEmbeddedTemplate
import com.djeremy.kafka.spring.test.annotation.IntegrationTest
import com.djeremy.process.monitor.embeded.kafka.FlowData.Companion.random
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.step.StepStreamsRegistry
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.Duration.ofSeconds

private const val COMMANDS_TOPIC = "m.commands.topic"
private const val EVENTS1_TOPIC = "m.events1.topic"
private const val EVENTS2_TOPIC = "m.events2.topic"
private const val EVENTS3_TOPIC = "m.events3.topic"
private const val CONFIGURATION_ID1 = "TestStepConfig1"
private const val CONFIGURATION_ID2 = "TestStepConfig2"

@IntegrationTest
@Import(TestConfig::class)
@EmbeddedKafka(partitions = 1,
        topics = [COMMANDS_TOPIC, EVENTS1_TOPIC, EVENTS2_TOPIC, EVENTS3_TOPIC, PROCESS_STEP_EVENTS_TOPIC],
brokerProperties = ["transaction.state.log.replication.factor=1", "transaction.state.log.min.isr=1"])
@ActiveProfiles(*["test", "multiple"])
internal class MultipleConfigurationFlowIT {
    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker
    private lateinit var kafkaEmbeddedTemplate: KafkaEmbeddedTemplate

    @Value("\${djeremy.kafka.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    @Autowired
    private lateinit var stepRepository: MongoRepository<StepDao, String>

    @Autowired
    private lateinit var stepStreamsRegistry: StepStreamsRegistry

    @Autowired
    private lateinit var applicationStreamsRegistry: ApplicationStreamsRegistry

    @BeforeEach
    fun setUp() {
        kafkaEmbeddedTemplate = KafkaEmbeddedTemplate(embeddedKafkaBroker, schemaRegistryUrl)
        awaitUntilAllStreamsAreRunning(stepStreamsRegistry, applicationStreamsRegistry)
    }

    @Test
    fun `test both configurations are joined correctly`() {
        // given
        val flowData = random()
        // when
        flowData.sendCommand()
        flowData.sendEvent1()
        flowData.sendEvent2()
        flowData.sendEvent3()
        // then
        `wait for all steps to be processed`()

        `assert all events were received`(flowData)
    }

    @Test
    fun `test both configurations are joined correctly (randomly)`() {
        // given
        val numberOfProcesses = 25
        val flowDatas = (1..numberOfProcesses).map { random() }
        val shuffledCommands = flowDatas.shuffled()
        val shuffledEvents1 = flowDatas.shuffled()
        val shuffledEvents2 = flowDatas.shuffled()
        val shuffledEvents3 = flowDatas.shuffled()
        // when
        shuffledCommands.zip(shuffledEvents1).zip(shuffledEvents2).zip(shuffledEvents3)
                .map { it ->
                    val (other1, event3) = it
                    val (other2, event2) = other1
                    val (event1, command) = other2
                    event3.sendEvent2()
                    event2.sendEvent3()
                    event1.sendEvent1()
                    command.sendCommand()
                }
        // then
        `wait for all steps to be processed`(ofSeconds(60), ofSeconds(4),
                numberOfProcesses * 6, numberOfProcesses * 2)
        val resultSteps = `assert and receive all events`(ofSeconds(60),
                ofSeconds(4), numberOfProcesses * 6)

        assertThat(resultSteps.map { it.value() })
                .usingElementComparatorIgnoringFields("receivedAt", "stepId")
                .containsAll(flowDatas.flatMap {
                    listOf<SpecificRecord>(
                            it.toExpectedCommandStep(CONFIGURATION_ID1),
                            it.toExpectedEvent1Step(CONFIGURATION_ID1),
                            it.toExpectedEvent2Step(CONFIGURATION_ID1),
                            it.toExpectedEvent3Step(CONFIGURATION_ID1),
                            it.toExpectedEvent2Step(CONFIGURATION_ID2),
                            it.toExpectedEvent3Step(CONFIGURATION_ID2)
                    )
                })
    }

    fun `wait for all steps to be processed`(duration: Duration = ofSeconds(10),
                                             pollInterval: Duration = ofSeconds(2),
                                             expectedSize: Int = 6,
                                             expectedProcesses: Int = 2) {
        await.atMost(duration).withPollInterval(pollInterval).untilAsserted {
            val steps = stepRepository.findAll()
            assertThat(steps).hasSize(expectedSize)
                    .filteredOn { it.processInstance == null }
                    .hasSize(0)
            val processInstanceIds = steps.groupBy { it.processInstance }.keys
            assertThat(processInstanceIds).hasSize(expectedProcesses)
        }
    }

    fun `assert and receive all events`(duration: Duration = ofSeconds(10),
                                        pollInterval: Duration = ofSeconds(2),
                                        expectedSize: Int): List<ConsumerRecord<String, SpecificRecord>> {
        lateinit var fetch: List<ConsumerRecord<String, SpecificRecord>>
        await.atMost(duration).withPollInterval(pollInterval).untilAsserted {
            fetch = kafkaEmbeddedTemplate.fetch(PROCESS_STEP_EVENTS_TOPIC)
            assertThat(fetch).hasSize(expectedSize)
        }
        return fetch
    }

    fun `assert all events were received`(flowData: FlowData) = await.untilAsserted {
        val fetch = kafkaEmbeddedTemplate.fetch(PROCESS_STEP_EVENTS_TOPIC)
        assertThat(fetch.map { it.value() }).hasSize(6)
                .usingElementComparatorIgnoringFields("receivedAt", "stepId")
                .containsAll(listOf<SpecificRecord>(
                        flowData.toExpectedCommandStep(CONFIGURATION_ID1),
                        flowData.toExpectedEvent1Step(CONFIGURATION_ID1),
                        flowData.toExpectedEvent2Step(CONFIGURATION_ID1),
                        flowData.toExpectedEvent3Step(CONFIGURATION_ID1),
                        flowData.toExpectedEvent2Step(CONFIGURATION_ID2),
                        flowData.toExpectedEvent3Step(CONFIGURATION_ID2))
                )
    }

    fun FlowData.sendCommand() = sendCommand(kafkaEmbeddedTemplate, COMMANDS_TOPIC)
    fun FlowData.sendEvent1() = sendEvent1(kafkaEmbeddedTemplate, EVENTS1_TOPIC)
    fun FlowData.sendEvent2() = sendEvent2(kafkaEmbeddedTemplate, EVENTS2_TOPIC)
    fun FlowData.sendEvent3() = sendEvent3(kafkaEmbeddedTemplate, EVENTS3_TOPIC)
}