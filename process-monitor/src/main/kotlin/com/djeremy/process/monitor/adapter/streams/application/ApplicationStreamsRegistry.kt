package com.djeremy.process.monitor.adapter.streams.application

import com.djeremy.process.monitor.adapter.streams.StreamDefinition
import org.apache.kafka.streams.KafkaStreams

interface ApplicationStreamsRegistry {
    fun add(applicationId: String, kafkaStreams: KafkaStreams)
    fun startByApplicationId(applicationId: String)
    fun stopByApplicationId(applicationId: String)
    fun isRegisteredByApplicationId(applicationId: String): Boolean
    fun getAllDeclaredApplicationIds(): Set<String>

    fun getAllKafkaStreams(): List<StreamDefinition>
    fun stopAllAndClean()
    fun startAll()
}
