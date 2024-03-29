package com.djeremy.process.monitor.adapter.streams.step

import com.djeremy.process.monitor.adapter.streams.KeyBasedStreamsRegistry
import com.djeremy.process.monitor.domain.transformation.TopicTransformations
import com.djeremy.process.monitor.adapter.streams.StreamDefinition
import org.apache.kafka.streams.KafkaStreams
import org.springframework.beans.factory.DisposableBean

open class DefaultStepStreamsRegistry : StepStreamsRegistry, DisposableBean {
    private val registry = KeyBasedStreamsRegistry()

    override fun add(topicTransformations: TopicTransformations, kafkaStreams: KafkaStreams) = registry.add(topicTransformations.topic, kafkaStreams)

    override fun startByTopic(topic: String) = registry.startStream(topic)

    override fun stopByTopic(topic: String) = registry.stopStream(topic)

    override fun isRegisteredByTopic(topic: String): Boolean = registry.isRegistered(topic)

    override fun getAllDeclaredTopics(): Set<String> = registry.getKeys()

    override fun getAllKafkaStreams(): List<StreamDefinition> = registry.getStreams()

    override fun stopAllAndClean() = registry.stopAll()

    override fun startAll() = registry.startAll()

    override fun destroy() = registry.stopAll()

}