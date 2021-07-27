package com.djeremy.process.monitor.adapter.health

import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamsRegistry
import com.djeremy.process.monitor.domain.streams.CustomizedKafkaProperties
import com.djeremy.process.monitor.domain.streams.step.StepStreamsRegistry
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG
import org.apache.kafka.streams.processor.TaskMetadata
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import java.lang.reflect.Method
import java.time.Duration
import java.util.HashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Collectors

class KafkaStreamsHealthIndicator(
    private val stepStreamsRegistry: StepStreamsRegistry,
    private val applicationStreamsRegistry: ApplicationStreamsRegistry,
    private val customizedKafkaProperties: CustomizedKafkaProperties) : AbstractHealthIndicator("Kafka-streams health check failed"), DisposableBean {
    companion object {
        private val CLASS_LOADER = KafkaStreamsHealthIndicator::class.java.classLoader
        private var isKafkaStreams25 = true
        private var methodForIsRunning: Method? = null
        private val healthStatusThreadLocal = ThreadLocal<Status>()
        private fun taskDetails(taskMetadata: Set<TaskMetadata>): Map<String, Any?> {
            val details: MutableMap<String, Any?> = HashMap()
            for (metadata in taskMetadata) {
                details["taskId"] = metadata.taskId()
                if (details.containsKey("partitions")) {
                    @Suppress("UNCHECKED_CAST") val partitionsInfo = details["partitions"] as MutableList<String>?
                    partitionsInfo?.addAll(addPartitionsInfo(metadata))
                } else {
                    details["partitions"] = addPartitionsInfo(metadata)
                }
            }
            return details
        }

        private fun addPartitionsInfo(metadata: TaskMetadata): List<String> {
            return metadata.topicPartitions().stream().map { p: TopicPartition -> "partition=" + p.partition() + ", topic=" + p.topic() }
                    .collect(Collectors.toList())
        }

        init {
            try {
                val KAFKA_STREAMS_STATE_CLASS = CLASS_LOADER.loadClass("org.apache.kafka.streams.KafkaStreams\$State")
                val declaredMethods = KAFKA_STREAMS_STATE_CLASS.declaredMethods
                for (m in declaredMethods) {
                    if (m.name == "isRunning") {
                        isKafkaStreams25 = false
                        methodForIsRunning = m
                    }
                }
            } catch (e: ClassNotFoundException) {
                throw IllegalStateException("KafkaStreams\$State class not found", e)
            }
        }
    }

    private val adminClientProperties: Map<String, Any> = customizedKafkaProperties.kafkaProperties.buildAdminProperties()
    private var adminClient: AdminClient? = null
    private val lock: Lock = ReentrantLock()

    override fun doHealthCheck(builder: Health.Builder) {
        try {
            lock.lock()
            if (adminClient == null) {
                adminClient = AdminClient.create(adminClientProperties)
            }
            val status = healthStatusThreadLocal.get()
            if (status === Status.DOWN) {
                builder.withDetail("No topic information available", "Kafka broker is not reachable")
                builder.status(Status.DOWN)
            } else {
                val listTopicsResult = adminClient!!.listTopics()
                listTopicsResult.listings()[60, TimeUnit.SECONDS]
                val stepUp = stepStreamsRegistryHealth(builder)
                val applicationUp = applicationStreamsRegistryHealth(builder)

                builder.status(if (stepUp && applicationUp) Status.UP else Status.DOWN)
            }
        } catch (e: Exception) {
            builder.withDetail("No topic information available", "Kafka broker is not reachable")
            builder.status(Status.DOWN)
            builder.withException(e)
            healthStatusThreadLocal.set(Status.DOWN)
        } finally {
            lock.unlock()
        }
    }

    private fun stepStreamsRegistryHealth(builder: Health.Builder): Boolean {
        var up = true
        for ((topic, kStream) in stepStreamsRegistry.getAllKafkaStreams()) {
            up = if (isKafkaStreams25) {
                up and kStream.state().isRunningOrRebalancing
            } else {
                // if Kafka client version is lower than 2.5, then call the method reflectively.
                val isRuningInvokedResult = methodForIsRunning!!.invoke(kStream.state()) as Boolean
                up and isRuningInvokedResult
            }
            builder.withDetails(buildDetails(kStream,
                    customizedKafkaProperties.buildStreamsPropertiesForTopic(topic)[APPLICATION_ID_CONFIG] as? String))
        }
        return up
    }

    private fun applicationStreamsRegistryHealth(builder: Health.Builder): Boolean {
        var up = true
        for ((applicationId, kStream) in applicationStreamsRegistry.getAllKafkaStreams()) {
            up = if (isKafkaStreams25) {
                up and kStream.state().isRunningOrRebalancing
            } else {
                // if Kafka client version is lower than 2.5, then call the method reflectively.
                val isRunningInvokedResult = methodForIsRunning!!.invoke(kStream.state()) as Boolean
                up and isRunningInvokedResult
            }
            builder.withDetails(buildDetails(kStream,
                    customizedKafkaProperties.buildStreamsPropertiesForApplication(applicationId)[APPLICATION_ID_CONFIG] as? String))
        }
        return up
    }

    private fun buildDetails(kafkaStreams: KafkaStreams, customizeApplicationId: String?): Map<String, Any> {
        val perAppIdName: MutableMap<String, Any> = mutableMapOf()
        val perThreadName: MutableMap<String, Any> = HashMap()
        val isRunningResult: Boolean = if (isKafkaStreams25) {
            kafkaStreams.state().isRunningOrRebalancing
        } else {
            // if Kafka client version is lower than 2.5, then call the method reflectively.
            methodForIsRunning!!.invoke(kafkaStreams.state()) as Boolean
        }
        if (isRunningResult) {
            for (metadata in kafkaStreams.localThreadsMetadata()) {
                val singleThreadData: MutableMap<String, Any> = HashMap()
                singleThreadData["threadName"] = metadata.threadName()
                singleThreadData["threadState"] = metadata.threadState()
                singleThreadData["adminClientId"] = metadata.adminClientId()
                singleThreadData["consumerClientId"] = metadata.consumerClientId()
                singleThreadData["restoreConsumerClientId"] = metadata.restoreConsumerClientId()
                singleThreadData["producerClientIds"] = metadata.producerClientIds()
                singleThreadData["activeTasks"] = taskDetails(metadata.activeTasks())
                singleThreadData["standbyTasks"] = taskDetails(metadata.standbyTasks())

                perThreadName[metadata.threadName()] = singleThreadData
            }
            perAppIdName[customizeApplicationId ?: "unknown"] = perThreadName
        } else {
            perThreadName[customizeApplicationId
                    ?: "unknown"] = String.format("The processor with application.id %s is down",
                    kafkaStreams.localThreadsMetadata().map { it.threadName() })
        }
        return perAppIdName
    }

    override fun destroy() {
        if (adminClient != null) {
            adminClient!!.close(Duration.ofSeconds(0))
        }
    }

}
