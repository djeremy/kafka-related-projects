package com.djeremy.process.monitor.config.adapter

import com.djeremy.process.monitor.adapter.store.ProcessConfigurationMongoAdapter
import com.djeremy.process.monitor.adapter.store.ProcessInstanceStateMongoAdapter
import com.djeremy.process.monitor.adapter.store.StepConfigurationMongoAdapter
import com.djeremy.process.monitor.adapter.store.StepMongoAdapter
import com.djeremy.process.monitor.adapter.store.mongo.ProcessConfigurationMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.StepMongoRepository
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StoreConfiguration {

    @Bean
    fun stepRepositoryV2(stepMongoRepository: StepMongoRepository): StepRepository = StepMongoAdapter(stepMongoRepository)

    @Bean
    fun stepConfigurationRepository(stepConfigurationMongoRepository: StepConfigurationMongoRepository): StepConfigurationRepository =
            StepConfigurationMongoAdapter(stepConfigurationMongoRepository)

    @Bean
    fun processConfigurationRepository(processConfigurationMongoRepository: ProcessConfigurationMongoRepository): ProcessConfigurationRepository =
            ProcessConfigurationMongoAdapter(processConfigurationMongoRepository)

    @Bean
    fun processInstanceStateRepository(processInstanceStateMongoRepository: ProcessInstanceStateMongoRepository): ProcessInstanceStateRepository =
            ProcessInstanceStateMongoAdapter(processInstanceStateMongoRepository)
}