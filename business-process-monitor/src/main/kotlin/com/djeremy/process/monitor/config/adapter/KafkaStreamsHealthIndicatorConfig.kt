package com.djeremy.process.monitor.config.adapter

import com.djeremy.process.monitor.adapter.health.KafkaStreamsHealthIndicator
import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.CustomizedKafkaProperties
import com.djeremy.process.monitor.adapter.streams.step.StepStreamsRegistry
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaStreamsHealthIndicatorConfig {

    @Bean
    fun kafkaStreamsHealthIndicator(stepStreamsRegistry: StepStreamsRegistry,
                                    applicationStreamsRegistry: ApplicationStreamsRegistry,
                                    customizedKafkaProperties: CustomizedKafkaProperties): HealthIndicator =
            KafkaStreamsHealthIndicator(stepStreamsRegistry, applicationStreamsRegistry, customizedKafkaProperties)
}