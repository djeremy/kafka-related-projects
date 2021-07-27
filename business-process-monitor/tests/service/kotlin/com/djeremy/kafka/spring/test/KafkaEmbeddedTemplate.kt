package com.djeremy.kafka.spring.test

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.subject.RecordNameStrategy
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.util.concurrent.ListenableFuture
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

private const val SCHEMA_REGISTRY_CONFIG = "schema.registry.url"
private const val AUTO_OFFSET_RESET_EARLIEST = "earliest"

@Suppress("unused")
open class KafkaEmbeddedTemplate {

    protected val embeddedKafkaBroker: EmbeddedKafkaBroker
    protected val kafkaTemplate: KafkaTemplate<String, SpecificRecord>
    protected val consumerFactory: ConsumerFactory<String, SpecificRecord>

    /**
     * Scheme schemaRegistryUrl should have the same mockedScope as used in application-test.yaml for arbitrary binders. Otherwise,
     * mocked schema registry client won't share cached schemas and serde will fail with serialization exception.
     * For example: mock://localhost:8081 or mock://dummy
     * It is necessary to have mock:// prefix in order to let serde to create mockSchemaRegistryClient
     */
    constructor(embeddedKafkaBroker: EmbeddedKafkaBroker, schemaRegistryUrl: String) {
        this.embeddedKafkaBroker = embeddedKafkaBroker
        this.kafkaTemplate = createKafkaTemplate(embeddedKafkaBroker, schemaRegistryUrl)
        this.consumerFactory = createConsumerFactory(embeddedKafkaBroker, schemaRegistryUrl)
    }

    constructor(embeddedKafkaBroker: EmbeddedKafkaBroker, kafkaTemplate: KafkaTemplate<String, SpecificRecord>, consumerFactory: ConsumerFactory<String, SpecificRecord>) {
        this.embeddedKafkaBroker = embeddedKafkaBroker
        this.kafkaTemplate = kafkaTemplate
        this.consumerFactory = consumerFactory
    }

    open fun send(record: ProducerRecord<String, SpecificRecord>): ListenableFuture<SendResult<String, SpecificRecord>> {
        logger.info { "Publishing the message: $record" }
        return kafkaTemplate.send(record)
    }
    open fun send(topic: String, key: String, data: SpecificRecord): ListenableFuture<SendResult<String, SpecificRecord>> {
        logger.info { "Publishing the message to the TOPIC [$topic] with KEY [$key] and PAYLOAD: [$data]" }
        return kafkaTemplate.send(topic, key, data)
    }

    @Deprecated("Deprecated", ReplaceWith("fetch(topic, timeout = Duration.ofMillis(timeout))", "java.time.Duration"))
    open fun fetch(topic: String, timeout: Long) =
        fetch(topic, timeout = Duration.ofMillis(timeout))

    open fun fetch(topic: String, minRecords: Int = -1, timeout: Duration = Duration.ofSeconds(10)): List<ConsumerRecord<String, SpecificRecord>> {
        val groupId = UUID.randomUUID().toString()
        val clientIdSuffix = UUID.randomUUID().toString()
        val count = if (minRecords < 0) "all" else "at least $minRecords"
        logger.info { "Fetching $count messages from topic $topic" }
        consumerFactory.createConsumer(groupId, clientIdSuffix).use { consumer ->
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topic)
            return KafkaTestUtils.getRecords(consumer, timeout.toMillis(), minRecords).toList()
        }
    }

    protected open fun createKafkaTemplate(embeddedKafkaBroker: EmbeddedKafkaBroker, schemaRegistryUrl: String): KafkaTemplate<String, SpecificRecord> {
        val producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker.brokersAsString)
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        producerProps[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = "10000"
        producerProps[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = "5000"
        producerProps[SCHEMA_REGISTRY_CONFIG] = schemaRegistryUrl
        producerProps[VALUE_SUBJECT_NAME_STRATEGY] = RecordNameStrategy::class.java
        return KafkaTemplate(DefaultKafkaProducerFactory(producerProps), true)
    }

    protected open fun createConsumerFactory(embeddedKafkaBroker: EmbeddedKafkaBroker, schemaRegistryUrl: String): ConsumerFactory<String, SpecificRecord> {
        val consumerProps = KafkaTestUtils.consumerProps("test-group-" + UUID.randomUUID().toString(), "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = AUTO_OFFSET_RESET_EARLIEST
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        consumerProps[SCHEMA_REGISTRY_CONFIG] = schemaRegistryUrl
        consumerProps[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = "true"
        consumerProps[VALUE_SUBJECT_NAME_STRATEGY] = RecordNameStrategy::class.java
        return DefaultKafkaConsumerFactory(consumerProps)
    }
}
