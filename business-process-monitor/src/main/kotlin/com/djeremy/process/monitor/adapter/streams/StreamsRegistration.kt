package com.djeremy.process.monitor.adapter.streams

import org.apache.kafka.streams.KafkaStreams

interface StreamsRegistration {
    fun initialize(transformations: Set<TopicTransformations>)
    fun register(transformations: TopicTransformations)
    fun register(applicationId: String, kafkaStreams: KafkaStreams)
}