package com.djeremy.process.monitor.adapter.streams.application

import org.apache.kafka.streams.KafkaStreams

data class ApplicationStreamDefinition(
        val applicationId: String,
        val stream: KafkaStreams
)
