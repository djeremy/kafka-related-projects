package com.djeremy.process.monitor.embeded.v2.kafka

import com.djeremy.avro.business.process.monitor.v2.Reference
import com.djeremy.kafka.spring.test.KafkaEmbeddedTemplate
import com.djeremy.kafka.spring.test.annotation.IntegrationTest
import com.djeremy.process.monitor.embeded.v2.kafka.FlowData.Companion.random
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.domain.streams.step.StepStreamsRegistry
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
import java.time.Instant
import java.util.UUID
import com.djeremy.avro.business.process.monitor.v2.ProcessStep as ProcessStepV2

private const val COMMANDS_TOPIC = "commands.topic"
private const val EVENTS1_TOPIC = "events1.topic"
private const val EVENTS2_TOPIC = "events2.topic"
private const val EVENTS3_TOPIC = "events3.topic"
private const val CONFIGURATION_ID = "TestStepConfig"

@IntegrationTest
@Import(TestConfig::class)
@EmbeddedKafka(partitions = 1, topics = [COMMANDS_TOPIC, EVENTS1_TOPIC, EVENTS2_TOPIC, EVENTS3_TOPIC, PROCESS_STEP_EVENTS_TOPIC],
        brokerProperties = ["transaction.state.log.replication.factor=1", "transaction.state.log.min.isr=1"])
@ActiveProfiles(*["test", "single"])
internal class SingleConfigurationFlowTest {
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
    fun `should join all events correctly`() {
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
    fun `should join all events correctly in wrong order (1)`() {
        // given
        val flowData = random()
        // when
        flowData.sendEvent3()
        flowData.sendCommand()
        flowData.sendEvent1()
        flowData.sendEvent2()
        // then
        `wait for all steps to be processed`()

        `assert all events were received`(flowData)
    }

    @Test
    fun `should join all events correctly in wrong order (2)`() {
        // given
        val flowData = random()
        // when
        flowData.sendEvent2()
        flowData.sendEvent3()
        flowData.sendEvent1()
        flowData.sendCommand()
        // then
        `wait for all steps to be processed`()

        `assert all events were received`(flowData)
    }

    @Test
    fun `should join all events correctly when alternative is received`() {
        // given
        val flowData = random()
        // when
        flowData.sendEvent2()
        flowData.sendEvent1()
        flowData.sendCommand()
        flowData.sendEventAl3()
        // then
        `wait for all steps to be processed`()

        `assert all events were received (alternative)`(flowData)
    }

    @Test
    fun `should join all events correctly if some old step was received`() {
        // given
        val flowData = random()
        val oldProcessStep = `random ProcessStep`()

        kafkaEmbeddedTemplate.send(PROCESS_STEP_EVENTS_TOPIC, UUID.randomUUID().toString(), oldProcessStep)
        // when
        flowData.sendEvent2()
        flowData.sendEvent1()
        flowData.sendCommand()
        flowData.sendEventAl3()
        // then
        `wait for all steps to be processed`()
    }

    @Test
    fun `Step Configuration with multiple events`() {
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
        `wait for all steps to be processed`(ofSeconds(30), ofSeconds(3),
                numberOfProcesses * 4, numberOfProcesses)
        val resultSteps = `assert and receive all events`(ofSeconds(180),
                ofSeconds(10), numberOfProcesses * 4)

        assertThat(resultSteps.map { it.value() })
                .usingElementComparatorIgnoringFields("receivedAt", "stepId")
                .containsAll(flowDatas.flatMap {
                    listOf<SpecificRecord>(
                            it.toExpectedEvent1Step(),
                            it.toExpectedEvent2Step(),
                            it.toExpectedEvent3Step(),
                            it.toExpectedCommandStep())
                })
    }

    fun `wait for all steps to be processed`(duration: Duration = ofSeconds(10),
                                             pollInterval: Duration = ofSeconds(2),
                                             expectedSize: Int = 4,
                                             expectedProcesses: Int = 1) {
        await.atMost(duration).withPollInterval(pollInterval).untilAsserted {
            val steps = stepRepository.findAll()
            assertThat(steps).hasSize(expectedSize)
                    .filteredOn { it.processInstance == null }
                    .hasSize(0)
            val processInstanceIds = steps.groupBy { it.processInstance }.keys
            assertThat(processInstanceIds).hasSize(expectedProcesses)

            assertThat(steps.filter { it.isNewlyInstanceAssigned == true })
                    .hasSize(expectedSize)
                    .describedAs("All steps should has newly assigned process instance flag to true")
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
        assertThat(fetch.map { it.value() }).hasSize(4)
                .usingElementComparatorIgnoringFields("receivedAt", "stepId")
                .containsAll(listOf<SpecificRecord>(
                        flowData.toExpectedCommandStep(),
                        flowData.toExpectedEvent1Step(),
                        flowData.toExpectedEvent2Step(),
                        flowData.toExpectedEvent3Step())
                )
    }

    fun `assert all events were received (alternative)`(flowData: FlowData) = await.untilAsserted {
        val fetch = kafkaEmbeddedTemplate.fetch(PROCESS_STEP_EVENTS_TOPIC)
        assertThat(fetch.map { it.value() }).hasSize(4)
                .usingElementComparatorIgnoringFields("receivedAt", "stepId")
                .containsAll(listOf<SpecificRecord>(
                        flowData.toExpectedCommandStep(),
                        flowData.toExpectedEvent1Step(),
                        flowData.toExpectedEvent2Step(),
                        flowData.toExpectedEventAl3Step())
                )
    }

    fun FlowData.sendCommand() = sendCommand(kafkaEmbeddedTemplate, COMMANDS_TOPIC)
    fun FlowData.sendEvent1() = sendEvent1(kafkaEmbeddedTemplate, EVENTS1_TOPIC)
    fun FlowData.sendEvent2() = sendEvent2(kafkaEmbeddedTemplate, EVENTS2_TOPIC)
    fun FlowData.sendEvent3() = sendEvent3(kafkaEmbeddedTemplate, EVENTS3_TOPIC)
    fun FlowData.sendEventAl3() = sendEventAl3(kafkaEmbeddedTemplate, EVENTS3_TOPIC)

    fun FlowData.toExpectedCommandStep(): ProcessStepV2 = toExpectedCommandStep(CONFIGURATION_ID)
    fun FlowData.toExpectedEvent1Step(): ProcessStepV2 = toExpectedEvent1Step(CONFIGURATION_ID)
    fun FlowData.toExpectedEvent2Step(): ProcessStepV2 = toExpectedEvent2Step(CONFIGURATION_ID)
    fun FlowData.toExpectedEvent3Step(): ProcessStepV2 = toExpectedEvent3Step(CONFIGURATION_ID)
    fun FlowData.toExpectedEventAl3Step(): ProcessStepV2 = toExpectedEventAl3Step(CONFIGURATION_ID)

    fun `random ProcessStep`() = ProcessStepV2("configurationId",
            "ignored",
            UUID.randomUUID().toString(),
            listOf(Reference(UUID.randomUUID().toString(), "undefined")),
            false,
            Instant.now())
}