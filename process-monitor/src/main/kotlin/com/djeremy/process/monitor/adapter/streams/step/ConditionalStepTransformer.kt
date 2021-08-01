package com.djeremy.process.monitor.adapter.streams.step

import com.djeremy.avro.business.process.monitor.ProcessStep
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.KeyValue

interface ConditionalStepTransformer {
    fun shouldAccept(event: GenericRecord): Boolean
    fun transform(key: String, event: GenericRecord): KeyValue<String, ProcessStep>
}