package com.djeremy.process.monitor.adapter.streams.step

import com.djeremy.process.monitor.domain.transformation.TopicTransformations
import com.djeremy.process.monitor.adapter.streams.StreamDefinition
import org.apache.kafka.streams.KafkaStreams

interface StepStreamsRegistry {
    fun add(topicTransformations: TopicTransformations, kafkaStreams: KafkaStreams)
    fun startByTopic(topic: String)
    fun stopByTopic(topic: String)
    fun isRegisteredByTopic(topic: String): Boolean
    fun getAllDeclaredTopics(): Set<String>

    fun getAllKafkaStreams(): List<StreamDefinition>
    fun stopAllAndClean()
    fun startAll()
}
