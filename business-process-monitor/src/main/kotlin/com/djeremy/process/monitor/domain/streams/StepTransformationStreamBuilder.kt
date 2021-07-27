package com.djeremy.process.monitor.domain.streams

import mu.KLogger
import mu.KotlinLogging.logger
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder

class StepTransformationStreamBuilder(
        private val stepEventTopic: String,
        private val kafkaProperties: CustomizedKafkaProperties
) {
    val logger: KLogger = logger{}

    fun buildStreamFor(transformations: TopicTransformations): KafkaStreams {
        val props = kafkaProperties.buildStreamsPropertiesForTopic(transformations.topic)

        val topology = with(StreamsBuilder()) {
            stream<String, GenericRecord>(transformations.topic)
                    .flatMap { key, value ->
                        logger.debug { "Received record from topic [${transformations.topic}] with key [${key}] " +
                                "and schemaName [${value.schema.fullName}]" }
                        transformations.filter { transformer ->
                            transformer.shouldAccept(value)
                        }.map { transformer ->
                            transformer.transform(key, value)
                        }

                    }.to(stepEventTopic)
            build()
        }
        return KafkaStreams(topology, props)
    }
}