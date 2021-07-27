package com.djeremy.process.monitor.domain.streams

import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.domain.streams.step.StepStreamsRegistry
import mu.KotlinLogging.logger
import org.apache.kafka.streams.KafkaStreams

class DefaultStreamsRegistration(
    private val stepStreamsRegistry: StepStreamsRegistry,
    private val applicationStreamRegistry: ApplicationStreamsRegistry,
    private val stepTransformationStreamBuilder: StepTransformationStreamBuilder
) : StreamsRegistration {

    private val log = logger { }

    override fun initialize(transformations: Set<TopicTransformations>) {
        stepStreamsRegistry.stopAllAndClean().run { log.info { "Stopping all running streams" } }

        transformations.forEach { topicTransformations ->
            stepStreamsRegistry.add(topicTransformations, stepTransformationStreamBuilder.buildStreamFor(topicTransformations))
        }

        stepStreamsRegistry.startAll().run { log.info { "Starting all streams" } }
    }

    override fun register(transformations: TopicTransformations) = with(transformations) {
        if (stepStreamsRegistry.isRegisteredByTopic(topic)) {
            stepStreamsRegistry.stopByTopic(topic)

            stepStreamsRegistry.add(this, stepTransformationStreamBuilder.buildStreamFor(this))
            stepStreamsRegistry.startByTopic(topic)
        } else {
            stepStreamsRegistry.add(this, stepTransformationStreamBuilder.buildStreamFor(this))
            stepStreamsRegistry.startByTopic(this.topic)
        }
    }

    override fun register(applicationId: String, kafkaStreams: KafkaStreams) {
        if (applicationStreamRegistry.isRegisteredByApplicationId(applicationId)) {
            applicationStreamRegistry.stopByApplicationId(applicationId)

            addAndStartByApplicationId(applicationId, kafkaStreams)
        } else {
            addAndStartByApplicationId(applicationId, kafkaStreams)
        }
    }

    private fun addAndStartByApplicationId(applicationId: String, kafkaStreams: KafkaStreams) {
        applicationStreamRegistry.add(applicationId, kafkaStreams)
        applicationStreamRegistry.startByApplicationId(applicationId)
    }
}