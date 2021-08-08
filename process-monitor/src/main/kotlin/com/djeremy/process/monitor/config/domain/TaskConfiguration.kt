package com.djeremy.process.monitor.config.domain

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.ProcessInstanceStateService
import com.djeremy.process.monitor.domain.process.StepService
import com.djeremy.process.monitor.domain.task.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration

@Configuration
class TaskConfiguration {

    @Bean
    fun processInstanceStateTask(
        processInstanceStateService: ProcessInstanceStateService,
        stepService: StepService
    ): ProcessInstanceStateTask = DefaultProcessInstanceStateTask(processInstanceStateService, stepService)

    @Bean
    @Profile(value = ["!test"])
    fun processExpiredAlert(
        processConfigurationRepository: ProcessConfigurationRepository,
        stepConfigurationRepository: StepConfigurationRepository
    ): ProcessExpiredAlert {
        val logger = KotlinLogging.logger(ProcessExpiredFormatToStringAlert::class.qualifiedName!!)
        return ProcessExpiredFormatToStringAlert(
            processConfigurationRepository,
            stepConfigurationRepository,
            { logger.warn(it) },
            { logger.error(it) }
        )
    }

    @Bean
    fun processInstanceExpiredTask(
        processInstanceStateRepository: ProcessInstanceStateRepository,
        processConfigurationRepository: ProcessConfigurationRepository,
        processExpiredAlert: ProcessExpiredAlert,
        @Value("\${process.instance.state.task.lag.duration}") lag: Duration
    ): ProcessInstanceExpiredTask =
        DefaultProcessInstanceExpiredTask(
            processInstanceStateRepository,
            processConfigurationRepository,
            processExpiredAlert,
            lag,
            pageSize = 500
        )
}