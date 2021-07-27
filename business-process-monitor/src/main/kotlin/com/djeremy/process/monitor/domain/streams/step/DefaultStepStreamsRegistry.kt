package com.djeremy.process.monitor.domain.streams.step

import com.djeremy.process.monitor.domain.streams.KeyBasedStreamsRegistry
import com.djeremy.process.monitor.domain.streams.step.StepStreamsRegistry
import com.djeremy.process.monitor.domain.streams.TopicTransformations
import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamDefinition
import org.apache.kafka.streams.KafkaStreams
import org.springframework.beans.factory.DisposableBean

open class DefaultStepStreamsRegistry : StepStreamsRegistry, DisposableBean {
    private val registry = KeyBasedStreamsRegistry()

    override fun add(topicTransformations: TopicTransformations, kafkaStreams: KafkaStreams) = registry.add(topicTransformations.topic, kafkaStreams)

    override fun startByTopic(topic: String) = registry.startStream(topic)

    override fun stopByTopic(topic: String) = registry.stopStream(topic)

    override fun isRegisteredByTopic(topic: String): Boolean = registry.isRegistered(topic)

    override fun getAllDeclaredTopics(): Set<String> = registry.getKeys()

    override fun getAllKafkaStreams(): List<ApplicationStreamDefinition> = registry.getStreams()

    override fun stopAllAndClean() = registry.stopAll()

    override fun startAll() = registry.startAll()

    override fun destroy() = registry.stopAll()

}