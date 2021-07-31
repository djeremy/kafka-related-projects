package com.djeremy.process.monitor.adapter.streams

import com.djeremy.process.monitor.adapter.streams.step.ConditionalStepTransformer


class TopicTransformations(
        val topic: String,
        private val transformers: Set<ConditionalStepTransformer>
) : Iterable<ConditionalStepTransformer> {

    override fun iterator(): Iterator<ConditionalStepTransformer> = transformers.iterator()

}