package com.djeremy.process.monitor.domain.streams

import com.djeremy.process.monitor.domain.streams.step.ConditionalStepTransformer


class TopicTransformations(
        val topic: String,
        private val transformers: Set<ConditionalStepTransformer>
) : Iterable<ConditionalStepTransformer> {

    override fun iterator(): Iterator<ConditionalStepTransformer> = transformers.iterator()

}