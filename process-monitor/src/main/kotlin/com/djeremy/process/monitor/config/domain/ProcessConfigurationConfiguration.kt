package com.djeremy.process.monitor.config.domain

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import com.djeremy.process.monitor.domain.process.DefaultProcessConfigurationService
import com.djeremy.process.monitor.domain.process.DefaultProcessInstanceStateAggregator
import com.djeremy.process.monitor.domain.process.DefaultStepConfigurationService
import com.djeremy.process.monitor.domain.process.DefaultStepService
import com.djeremy.process.monitor.domain.process.ProcessConfigurationService
import com.djeremy.process.monitor.domain.process.ProcessInstanceStateAggregator
import com.djeremy.process.monitor.domain.process.StepConfigurationService
import com.djeremy.process.monitor.domain.process.StepService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProcessConfigurationConfiguration {

    @Bean
    fun stepConfigurationServiceV2(stepConfigurationRepository: StepConfigurationRepository): StepConfigurationService =
            DefaultStepConfigurationService(stepConfigurationRepository)

    @Bean
    fun processConfigurationServiceV2(
            processConfigurationRepository: ProcessConfigurationRepository,
            stepConfigurationService: StepConfigurationService): ProcessConfigurationService =
            DefaultProcessConfigurationService(processConfigurationRepository, stepConfigurationService)

    @Bean
    fun stepServiceV2(
            repository: StepRepository,
            stepConfigurationRepository: StepConfigurationRepository): StepService =
            DefaultStepService(repository, stepConfigurationRepository)

    @Bean
    fun processInstanceStateService(processInstanceStateRepository: ProcessInstanceStateRepository): ProcessInstanceStateAggregator =
            DefaultProcessInstanceStateAggregator(processInstanceStateRepository)
}