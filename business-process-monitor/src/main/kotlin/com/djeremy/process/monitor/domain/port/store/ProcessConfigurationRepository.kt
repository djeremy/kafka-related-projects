package com.djeremy.process.monitor.domain.port.store

import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration

interface ProcessConfigurationRepository {
    fun getAll(): List<ProcessConfiguration>
    fun getBy(id: ProcessConfigurationId): ProcessConfiguration?
    fun save(processConfiguration: ProcessConfiguration)
}