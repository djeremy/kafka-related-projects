package com.djeremy.publisher

import com.djeremy.avro.process.CreateProcess
import com.djeremy.avro.process.ProcessCreated
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import java.lang.Exception

fun main() {

    val generateData = generateData(100)

    createProducer("test-processCreated-configuration").use { producer ->
        producer.initTransactions()
        generateData
            .chunked(1000)
            .forEach {
                runCatching {
                    producer.beginTransaction()

                    it.forEach(producer::sendCommandOrEvent)

                    producer.commitTransaction()
                }.onFailure {
                    when (it) {
                        is KafkaException -> producer.abortTransaction()
                        is Exception -> throw it
                    }
                }
            }
    }
}

private fun KafkaProducer<String, SpecificRecord>.sendCommandOrEvent(
    record: Pair<String, SpecificRecord>
) {
    when (record.second.schema) {
        CreateProcess.`SCHEMA$` -> send(ProducerRecord("process.create.commands", record.first, record.second))
        ProcessCreated.`SCHEMA$` -> send(ProducerRecord("process.create.events", record.first, record.second))
    }
}