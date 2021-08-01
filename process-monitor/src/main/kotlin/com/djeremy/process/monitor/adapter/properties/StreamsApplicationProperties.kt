package com.djeremy.process.monitor.adapter.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import com.djeremy.process.monitor.adapter.streams.ApplicationStreamKafkaProperties as ApplicationStreamKafkaPropertiesModel

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("djeremy.kafka.system.monitor.streams.config")
@Validated
class ApplicationStreamKafkaProperties(
        var defaultValueSerde: String? = null,
        var processingGuarantee: String? = null)


fun ApplicationStreamKafkaProperties.toModel(): ApplicationStreamKafkaPropertiesModel =
        ApplicationStreamKafkaPropertiesModel(defaultValueSerde, processingGuarantee)