package com.djeremy.splitter

import org.apache.kafka.streams.StreamsConfig
import java.util.Properties
import kotlin.reflect.KClass

fun properties(clazz: KClass<*>): Properties {
    val properties = Properties()
    properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, clazz.simpleName)
    properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "0.0.0.0")
    return properties
}
