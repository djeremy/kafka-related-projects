package com.djeremy.process.monitor.domain.port.store

import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import com.djeremy.process.monitor.domain.process.models.StepId

interface StepConfigurationRepository {
    fun getById(configurationId: ProcessConfigurationId): List<StepConfigurationModel>
    fun getById(id: StepId): StepConfigurationModel?
    fun save(stepConfiguration: StepConfigurationModel)
    fun saveAll(stepConfigurations: List<StepConfigurationModel>)
    fun delete(stepConfigurations: List<StepConfigurationModel>)
}