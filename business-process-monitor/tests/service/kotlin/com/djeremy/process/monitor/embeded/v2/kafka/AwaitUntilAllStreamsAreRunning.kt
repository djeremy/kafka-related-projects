package com.djeremy.process.monitor.embeded.v2.kafka

import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.domain.streams.step.StepStreamsRegistry
import org.apache.kafka.streams.KafkaStreams.State.RUNNING
import org.awaitility.kotlin.await

fun awaitUntilAllStreamsAreRunning(stepStreamsRegistry: StepStreamsRegistry, applicationStreamsRegistry: ApplicationStreamsRegistry) {
    await.until {
        stepStreamsRegistry.getAllKafkaStreams().stream().allMatch { t -> t.stream.state() == RUNNING } &&
                applicationStreamsRegistry.getAllKafkaStreams().stream()
                    .allMatch { t -> t.stream.state() == RUNNING }
    }
}
