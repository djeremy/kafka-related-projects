package com.djeremy.process.monitor.domain.port.store

interface ProcessConfigurationLoader<T> {

    fun loadFrom(from: T)

}