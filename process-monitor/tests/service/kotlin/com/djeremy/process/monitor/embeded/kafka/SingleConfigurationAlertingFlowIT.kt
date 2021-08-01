package com.djeremy.process.monitor.embeded.kafka

import com.djeremy.avro.business.process.monitor.ProcessStep
import com.djeremy.kafka.spring.test.KafkaEmbeddedTemplate
import com.djeremy.kafka.spring.test.annotation.IntegrationTest
import com.djeremy.process.monitor.embeded.kafka.FlowData.Companion.random
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceDao
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStage
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateDao
import com.djeremy.process.monitor.adapter.store.mongo.StepDao
import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.step.StepStreamsRegistry
import com.djeremy.process.monitor.domain.task.ProcessInstanceExpiredTask
import com.djeremy.process.monitor.domain.task.ProcessInstanceStateTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.avro.specific.SpecificRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.Duration.ofSeconds

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
internal class SingleConfigurationAlertingFlowIT {
    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker
    private lateinit var kafkaEmbeddedTemplate: KafkaEmbeddedTemplate

    @Value("\${djeremy.kafka.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    @Autowired
    private lateinit var stepRepository: MongoRepository<StepDao, String>

    @Autowired
    private lateinit var processInstanceStateTask: ProcessInstanceStateTask

    @Autowired
    private lateinit var processInstanceExpiredTask: ProcessInstanceExpiredTask

    @Autowired
    @Qualifier("testAlert")
    private lateinit var alert: MockLoggingProcessExpiredAlert

    @Autowired
    private lateinit var processInstanceStateRepository: MongoRepository<ProcessInstanceStateDao, ProcessInstanceDao>

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
        runBlocking {
            // given
            val flowData = random()
            flowData.sendCommand()

            // produce delay
            delay(2000)

            flowData.sendEvent1()
            flowData.sendEvent2()
            flowData.sendEvent3()

            `wait for all steps to be processed`()
            `assert all events were received`(flowData)

            // when
            processInstanceStateTask.execute()
            processInstanceExpiredTask.execute()

            // then
            val result = processInstanceStateRepository.findAll()

            assertThat(result).hasSize(1)
            assertThat(result.first().stage)
                    .isEqualTo(ProcessInstanceStage(isFinished = true, isAdmitted = true))

            assertThat(alert.alerted)
                    .hasSize(1)
        }
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
                    .describedAs("All steps should has newly assigned process instance flat to true")
        }
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

    fun FlowData.sendCommand() = sendCommand(kafkaEmbeddedTemplate, COMMANDS_TOPIC)
    fun FlowData.sendEvent1() = sendEvent1(kafkaEmbeddedTemplate, EVENTS1_TOPIC)
    fun FlowData.sendEvent2() = sendEvent2(kafkaEmbeddedTemplate, EVENTS2_TOPIC)
    fun FlowData.sendEvent3() = sendEvent3(kafkaEmbeddedTemplate, EVENTS3_TOPIC)

    fun FlowData.toExpectedCommandStep(): ProcessStep = toExpectedCommandStep(CONFIGURATION_ID)
    fun FlowData.toExpectedEvent1Step(): ProcessStep = toExpectedEvent1Step(CONFIGURATION_ID)
    fun FlowData.toExpectedEvent2Step(): ProcessStep = toExpectedEvent2Step(CONFIGURATION_ID)
    fun FlowData.toExpectedEvent3Step(): ProcessStep = toExpectedEvent3Step(CONFIGURATION_ID)

}