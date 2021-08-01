package com.djeremy.process.monitor.embeded.kafka

import com.djeremy.avro.business.process.monitor.ProcessStep
import com.djeremy.avro.business.process.monitor.Reference
import com.djeremy.avro.test.v2.AlternativeEvent3
import com.djeremy.avro.test.v2.Command
import com.djeremy.avro.test.v2.Event1
import com.djeremy.avro.test.v2.Event2
import com.djeremy.avro.test.v2.Event3
import com.djeremy.kafka.spring.test.KafkaEmbeddedTemplate
import com.djeremy.process.monitor.randomUUID
import java.time.Instant

const val PROCESS_STEP_EVENTS_TOPIC = "business-process.step.events"

class FlowData(val command: Command,
               val event1: Event1,
               val event2: Event2,
               val event3: Event3,
               val eventAl3: AlternativeEvent3) {
    companion object {
        fun random(): FlowData {
            val commandId = randomUUID()
            val event1Id = randomUUID()
            val event2Id = randomUUID()
            val event3Id = randomUUID()
            return FlowData(command = Command(commandId, event1Id, event2Id),
                    event1 = Event1(event1Id),
                    event2 = Event2(event2Id, event3Id),
                    event3 = Event3(event3Id),
                    eventAl3 = AlternativeEvent3(event3Id))
        }
    }

    fun sendCommand(kafkaEmbeddedTemplate: KafkaEmbeddedTemplate, topicName: String) = kafkaEmbeddedTemplate.send(topicName, command.getId().getValue(), command)
    fun sendEvent1(kafkaEmbeddedTemplate: KafkaEmbeddedTemplate, topicName: String) = kafkaEmbeddedTemplate.send(topicName, event1.getId().getValue(), event1)
    fun sendEvent2(kafkaEmbeddedTemplate: KafkaEmbeddedTemplate, topicName: String) = kafkaEmbeddedTemplate.send(topicName, event2.getId().getValue(), event2)
    fun sendEvent3(kafkaEmbeddedTemplate: KafkaEmbeddedTemplate, topicName: String) = kafkaEmbeddedTemplate.send(topicName, event3.getId().getValue(), event3)
    fun sendEventAl3(kafkaEmbeddedTemplate: KafkaEmbeddedTemplate, topicName: String) = kafkaEmbeddedTemplate.send(topicName, eventAl3.getId().getValue(), eventAl3)

    fun toExpectedCommandStep(configurationId: String): ProcessStep = with(command) {
        ProcessStep(configurationId,
                "ignored",
                getId().getValue().toString(),
                listOf(Reference(event1.getId().getValue().toString(), "event1Id.value"),
                        Reference(event2.getId().getValue().toString(), "event2Id.value")),
                false,
                Instant.now())
    }

    fun toExpectedEvent1Step(configurationId: String): ProcessStep = with(event1) {
        ProcessStep(configurationId,
                "ignored",
                getId().getValue().toString(),
                listOf(Reference(getId().getValue().toString(), "eventId")),
                false,
                Instant.now())
    }

    fun toExpectedEvent2Step(configurationId: String): ProcessStep = with(event2) {
        ProcessStep(configurationId,
                "ignored",
                getId().getValue().toString(),
                listOf(Reference(getEvent3Id().getValue().toString(), "event3Id.value")),
                false,
                Instant.now())
    }

    fun toExpectedEvent3Step(configurationId: String): ProcessStep = with(event3) {
        ProcessStep(configurationId,
                "ignored",
                getId().getValue().toString(),
                listOf(Reference(getId().getValue().toString(), "eventId")),
                true,
                Instant.now())
    }

    fun toExpectedEventAl3Step(configurationId: String): ProcessStep = with(eventAl3) {
        ProcessStep(configurationId,
                "ignored",
                getId().getValue().toString(),
                listOf(Reference(getId().getValue().toString(), "eventId")),
                true,
                Instant.now())
    }
}