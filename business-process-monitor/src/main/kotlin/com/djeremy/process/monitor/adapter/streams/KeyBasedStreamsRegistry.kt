package com.djeremy.process.monitor.adapter.streams

import com.djeremy.process.monitor.adapter.streams.application.ApplicationStreamDefinition
import mu.KotlinLogging.logger
import org.apache.kafka.streams.KafkaStreams
import java.util.concurrent.ConcurrentHashMap

class KeyBasedStreamsRegistry {

    private val log = logger { }

    private var streams: MutableMap<String, KafkaStreams> = ConcurrentHashMap()

    fun add(key: String, kafkaStreams: KafkaStreams) {
        val oldStream = streams.putIfAbsent(key, kafkaStreams)
        if (oldStream != null) throw IllegalStateException("KafkaStreams already registered for given key ${key}. " +
                "Please use stopAndClean and then add. Or single stop and start method.")
    }

    fun startStream(key: String) {
        streams[key].let {
            if (it == null) {
                throw IllegalStateException("Cannot start Kafka Streams for key=[${key}] because it has not been registered.")
            } else {
                start(it)
            }
        }
    }

    fun stopStream(key: String) {
        streams[key].let {
            if (it == null) {
                throw IllegalStateException("Cannot stop Kafka Streams for key=[$key] because it has not been registered.")
            } else {
                it.close()
            }
        }
    }

    fun isRegistered(key: String): Boolean = streams[key] != null

    fun getKeys(): Set<String> = streams.keys

    fun getStreams(): List<ApplicationStreamDefinition> {
        return streams.map {
            ApplicationStreamDefinition(it.key, it.value)
        }
    }

    fun startAll() {
        streams.forEach { (_, kafkaStreams) ->
            start(kafkaStreams)
        }
    }

    private fun start(stream: KafkaStreams) {
        if (stream.state().isRunningOrRebalancing.not()) {
            log.info { "Starting stream" }
            stream.start()
            Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        } else {
            log.info { "Stream is already started" }
        }
    }

    fun stopAll() {
        streams.map { (_, value) -> value }.forEach { it.close() }
        streams.clear()
    }
}