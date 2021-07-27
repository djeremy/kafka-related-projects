package com.djeremy.process.monitor.adapter.startup

import com.djeremy.process.monitor.domain.process.ProcessConfigurationService
import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamDefinition
import com.djeremy.process.monitor.domain.streams.step.DefaultStepTransformer
import com.djeremy.process.monitor.domain.streams.StreamsRegistration
import com.djeremy.process.monitor.domain.streams.TopicTransformations
import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean

class StreamsStartup(
    private val configurationService: ProcessConfigurationService,
    private val applicationStreamsDefinitions: List<ApplicationStreamDefinition>,
    private val streamsRegistration: StreamsRegistration
) : InitializingBean {
    private val logger = KotlinLogging.logger {}

    override fun afterPropertiesSet() {
        logger.info { "Initializing step streams" }
        val topicTransformations = prepareTopicTransformations()
        streamsRegistration.initialize(topicTransformations)

        logger.info { "Initializing application streams" }
        applicationStreamsDefinitions.forEach {
            streamsRegistration.register(it.applicationId, it.stream)
        }
    }

    private fun prepareTopicTransformations() = configurationService.getAll()
            .flatMap { it.steps }
            .groupBy { it.getTopic() }
            .map { TopicTransformations(it.key, it.value.map(::DefaultStepTransformer).toSet()) }
            .toSet()
}