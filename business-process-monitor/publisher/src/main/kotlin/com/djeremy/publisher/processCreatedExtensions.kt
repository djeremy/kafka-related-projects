package com.djeremy.publisher

import com.djeremy.avro.process.CreateProcess
import com.djeremy.avro.process.ProcessCreated
import com.djeremy.avro.process.ProcessId
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.subject.RecordNameStrategy
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import java.util.UUID


// we are going to test 'processCreated' configuration defined in
// business process monitor yaml file.
fun createProducer(id: String): KafkaProducer<String, SpecificRecord> {
    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:19092"

    props["specific.avro.reader"] = true
    props["value.subject.name.strategy"] = RecordNameStrategy::class.java.name
    props["schema.registry.url"] = "http://localhost:19081"
    props["auto.register.schemas"] = "true"

    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.name
    props[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "test$id"
    props[ProducerConfig.LINGER_MS_CONFIG] = "100"
    props[ProducerConfig.BATCH_SIZE_CONFIG] = "64768"
    props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "zstd"

    return KafkaProducer(props)
}

fun generateData(times: Int = 1): List<Pair<String, SpecificRecord>> {
    return (0 until times).flatMap {
        val processId = ProcessId(UUID.randomUUID().toString());
        val createProcess = CreateProcess(processId)
        val processCreated = ProcessCreated(processId)
        listOf(processId.getValue() to createProcess, processId.getValue() to processCreated)
    }
}
