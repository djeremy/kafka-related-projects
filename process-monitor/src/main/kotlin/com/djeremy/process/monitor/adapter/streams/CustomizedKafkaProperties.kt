package com.djeremy.process.monitor.adapter.streams

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig.*
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import java.util.Properties

class CustomizedKafkaProperties(
        val kafkaProperties: KafkaProperties,
        private val applicationStreamKafkaProperties: ApplicationStreamKafkaProperties) {

    fun buildStreamsPropertiesForTopic(topic: String): Properties = with(kafkaProperties) {
        requireNotNull(streams.applicationId)
        val builtStreamsProperties = buildStreamsProperties()

        builtStreamsProperties.customizeApplicationId(topic)
        builtStreamsProperties.customizeClientId(topic)
        builtStreamsProperties.setAutoOffsetReset()

        return Properties(builtStreamsProperties.size).apply { putAll(builtStreamsProperties) }
    }

    fun buildStreamsPropertiesForApplication(applicationId: String): Properties = with(kafkaProperties) {
        requireNotNull(streams.applicationId)
        val builtStreamsProperties = buildStreamsProperties()

        builtStreamsProperties.customizeApplicationId(applicationId)
        builtStreamsProperties.customizeClientId(applicationId)

        applicationStreamKafkaProperties.defaultValueSerde?.apply { builtStreamsProperties[DEFAULT_VALUE_SERDE_CLASS_CONFIG] = this }
        applicationStreamKafkaProperties.processingGuarantee?.apply { builtStreamsProperties[PROCESSING_GUARANTEE_CONFIG] = this }

        return Properties(builtStreamsProperties.size).apply { putAll(builtStreamsProperties) }
    }

    companion object {
        fun MutableMap<String, Any>.customizeApplicationId(topic: String): Any? = computeIfPresent(APPLICATION_ID_CONFIG) { _, value -> "$value-$topic" }

        fun MutableMap<String, Any>.customizeClientId(topic: String) {
            computeIfPresent(ConsumerConfig.CLIENT_ID_CONFIG) { _, value -> "$value-$topic" }
        }

        fun MutableMap<String, Any>.setAutoOffsetReset() {
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
        }
    }
}

data class ApplicationStreamKafkaProperties(
        val defaultValueSerde: String? = null,
        val processingGuarantee: String? = null)