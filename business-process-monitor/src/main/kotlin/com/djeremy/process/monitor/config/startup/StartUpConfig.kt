package com.djeremy.process.monitor.config.startup

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationLoader
import com.djeremy.process.monitor.adapter.properties.ConfigurationPropertiesV2
import com.djeremy.process.monitor.adapter.properties.ProcessConfigurationPropertiesV2
import com.djeremy.process.monitor.adapter.startup.ConfigurationPropertiesLoaderV2
import com.djeremy.process.monitor.adapter.startup.MongoIndexStartup
import com.djeremy.process.monitor.adapter.startup.StreamsStartup
import com.djeremy.process.monitor.adapter.startup.lock.AtomicExecutor
import com.djeremy.process.monitor.domain.process.ProcessConfigurationService
import com.djeremy.process.monitor.domain.streams.application.ApplicationStreamDefinition
import com.djeremy.process.monitor.domain.streams.StreamsRegistration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class StartUpConfig {

    @Bean
    fun configurationPropertiesLoaderV2(configurationPropertiesV2: ConfigurationPropertiesV2,
                                        processConfigurationService: ProcessConfigurationService,
                                        startupLock: AtomicExecutor): ProcessConfigurationLoader<ProcessConfigurationPropertiesV2> =
            ConfigurationPropertiesLoaderV2(configurationPropertiesV2, processConfigurationService, startupLock)

    @Bean
    @DependsOn("configurationPropertiesLoaderV2")
    fun streamsStartup(processConfigurationService: ProcessConfigurationService, streamsRegistration: StreamsRegistration,
                       applicationStreamsDefinitions: List<ApplicationStreamDefinition>): StreamsStartup =
            StreamsStartup(processConfigurationService, applicationStreamsDefinitions, streamsRegistration)

    @Bean
    fun mongoIndexStartup(mongoTemplate: MongoTemplate): MongoIndexStartup = MongoIndexStartup(mongoTemplate)
}