package com.djeremy.process.monitor.domain.streams.application

import org.apache.kafka.streams.KafkaStreams

data class ApplicationStreamDefinition(
        val applicationId: String,
        val stream: KafkaStreams
)
