package com.djeremy.process.monitor.domain.transformation


// This class is used to aggregate StepTransformers for each topic
// as multiple steps can corresponds to same topic, but different event types
class TopicTransformations(
    val topic: String,
    private val transformers: Set<ConditionalStepTransformer>
) : Iterable<ConditionalStepTransformer> {

    override fun iterator(): Iterator<ConditionalStepTransformer> = transformers.iterator()

}