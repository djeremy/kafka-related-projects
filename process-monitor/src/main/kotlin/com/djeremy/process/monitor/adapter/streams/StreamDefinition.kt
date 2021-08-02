package com.djeremy.process.monitor.adapter.streams

import org.apache.kafka.streams.KafkaStreams

data class StreamDefinition(
        val internalApplicationId: String,
        val stream: KafkaStreams
)
