package com.djeremy.process.monitor.adapter.streams.application

import com.djeremy.process.monitor.adapter.streams.KeyBasedStreamsRegistry
import org.apache.kafka.streams.KafkaStreams
import org.springframework.beans.factory.DisposableBean

open class DefaultApplicationStreamsRegistry : ApplicationStreamsRegistry, DisposableBean {
    private val registry = KeyBasedStreamsRegistry()

    override fun add(applicationId: String, kafkaStreams: KafkaStreams) = registry.add(applicationId, kafkaStreams)

    override fun startByApplicationId(applicationId: String) = registry.startStream(applicationId)

    override fun stopByApplicationId(applicationId: String) = registry.stopStream(applicationId)

    override fun isRegisteredByApplicationId(applicationId: String) = registry.isRegistered(applicationId)

    override fun getAllDeclaredApplicationIds(): Set<String> = registry.getKeys()

    override fun getAllKafkaStreams(): List<ApplicationStreamDefinition> = registry.getStreams()

    override fun stopAllAndClean() = registry.stopAll()

    override fun startAll() = registry.startAll()

    override fun destroy() = registry.stopAll()
}