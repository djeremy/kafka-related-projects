package com.djeremy.splitter

import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import io.confluent.kafka.serializers.subject.RecordNameStrategy
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver

class RecordFactory(
    records: List<SpecificRecord>
) {
    private val client = MockSchemaRegistryClient()
    private val topicNameStrategy = RecordNameStrategy()

    private val commonSerde = SpecificAvroSerde<SpecificRecord>(client)
    val commonGenericSerde = GenericAvroSerde(client)

    init {
        records.map(SpecificRecord::getSchema).forEach {
            client.register(topicNameStrategy.subjectName("does-not-care", false, AvroSchema(it)), AvroSchema(it))
        }

        val config = mapOf(
            SPECIFIC_AVRO_READER_CONFIG to "true",
            SCHEMA_REGISTRY_URL_CONFIG to "mock://"
        )
        commonSerde.configure(config, false)
        commonGenericSerde.configure(config, false)
    }

    @SuppressWarnings("unchecked")
    fun <T> createInputTopic(driver: TopologyTestDriver, topic: String): TestInputTopic<String, T> {
        return driver.createInputTopic(
            topic,
            Serdes.String().serializer(),
            commonSerde.serializer()
        ) as TestInputTopic<String, T>
    }

    @SuppressWarnings("unchecked")
    fun createGenericInputTopic(driver: TopologyTestDriver, topic: String): TestInputTopic<String, GenericRecord> {
        return driver.createInputTopic(
            topic,
            Serdes.String().serializer(),
            commonGenericSerde.serializer()
        )
    }

    @SuppressWarnings("unchecked")
    fun <T> createOutputTopic(driver: TopologyTestDriver, topic: String): TestOutputTopic<String, T> {
        return driver.createOutputTopic(
            topic,
            Serdes.String().deserializer(),
            commonSerde.deserializer()
        ) as TestOutputTopic<String, T>
    }
}