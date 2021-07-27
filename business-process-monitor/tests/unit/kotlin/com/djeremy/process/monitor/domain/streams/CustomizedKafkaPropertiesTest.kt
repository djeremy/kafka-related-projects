package com.djeremy.process.monitor.domain.streams

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.kafka.KafkaProperties

class CustomizedKafkaPropertiesTest {
    lateinit var customizedKafkaProperties: CustomizedKafkaProperties

    @BeforeEach
    fun setup() {
        val kafkaProperties: KafkaProperties = mockk()
        val streams: KafkaProperties.Streams = mockk()
        every { kafkaProperties.streams } returns streams
        every { streams.applicationId } returns "applicationId"
        every { kafkaProperties.buildStreamsProperties() } returns mutableMapOf()

        customizedKafkaProperties = CustomizedKafkaProperties(kafkaProperties, ApplicationStreamKafkaProperties())
    }

    @Test
    fun `buildStreamsPropertiesForTopic should return autoOffsetReset property latest`() {
        assertThat(customizedKafkaProperties.buildStreamsPropertiesForTopic("topic")).contains(
            entry(
                "auto.offset.reset",
                "latest"
            )
        )
    }

    @Test
    fun `buildStreamsPropertiesForApplication should not return autoOffsetReset property latest`() {
        assertThat(customizedKafkaProperties.buildStreamsPropertiesForApplication("topic")).doesNotContain(
            entry(
                "auto.offset.reset",
                "latest"
            )
        )
    }
}
