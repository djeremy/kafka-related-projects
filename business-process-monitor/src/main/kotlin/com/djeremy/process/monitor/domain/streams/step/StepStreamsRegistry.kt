package com.djeremy.process.monitor.domain.streams.step

import com.djeremy.process.monitor.domain.streams.TopicTransformations
import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamDefinition
import org.apache.kafka.streams.KafkaStreams

interface StepStreamsRegistry {
    fun add(topicTransformations: TopicTransformations, kafkaStreams: KafkaStreams)
    fun startByTopic(topic: String)
    fun stopByTopic(topic: String)
    fun isRegisteredByTopic(topic: String): Boolean
    fun getAllDeclaredTopics(): Set<String>

    fun getAllKafkaStreams(): List<ApplicationStreamDefinition>
    fun stopAllAndClean()
    fun startAll()
}
