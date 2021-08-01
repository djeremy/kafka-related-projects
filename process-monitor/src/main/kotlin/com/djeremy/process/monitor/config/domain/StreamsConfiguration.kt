package com.djeremy.process.monitor.config.domain

import com.djeremy.process.monitor.adapter.properties.ApplicationStreamKafkaProperties
import com.djeremy.process.monitor.adapter.properties.toModel
import com.djeremy.process.monitor.domain.process.StepService
import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamDefinition
import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.CustomizedKafkaProperties
import com.djeremy.process.monitor.adapter.streams.application.DefaultApplicationStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.step.DefaultStepStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.DefaultStreamsRegistration
import com.djeremy.process.monitor.adapter.streams.ProcessStepStreamDefinitionFactory
import com.djeremy.process.monitor.adapter.streams.step.StepStreamsRegistry
import com.djeremy.process.monitor.adapter.streams.StepTransformationStreamBuilder
import com.djeremy.process.monitor.adapter.streams.StreamsRegistration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StreamsConfiguration {

    @Bean
    fun customizedKafkaProperties(kafkaProperties: KafkaProperties,
                                  applicationStreamKafkaProperties: ApplicationStreamKafkaProperties): CustomizedKafkaProperties =
            CustomizedKafkaProperties(kafkaProperties, applicationStreamKafkaProperties.toModel())

    @Bean
    fun applicationStreamsRegistry(): ApplicationStreamsRegistry = DefaultApplicationStreamsRegistry()

    @Bean
    fun stepStreamsRegistry(): StepStreamsRegistry = DefaultStepStreamsRegistry()

    @Bean
    fun stepTransformationStreamBuilder(@Value("\${djeremy.kafka.step.topic}") stepTopic: String,
                                        customizedKafkaProperties: CustomizedKafkaProperties): StepTransformationStreamBuilder =
            StepTransformationStreamBuilder(stepTopic, customizedKafkaProperties)

    @Bean
    fun streamsRegistration(stepStreamsRegistry: StepStreamsRegistry,
                            applicationStreamsRegistry: ApplicationStreamsRegistry,
                            stepTransformationStreamBuilder: StepTransformationStreamBuilder
    ): StreamsRegistration = DefaultStreamsRegistration(stepStreamsRegistry, applicationStreamsRegistry, stepTransformationStreamBuilder)

    @Bean(name = ["processStepStreamDefinition"])
    fun processStepStreamDefinition(
            @Value("\${djeremy.kafka.step.topic}") stepTopic: String,
            customizedKafkaProperties: CustomizedKafkaProperties,
            stepService: StepService
    ): ApplicationStreamDefinition = ProcessStepStreamDefinitionFactory(stepTopic, stepTopic, customizedKafkaProperties, stepService).build()
}