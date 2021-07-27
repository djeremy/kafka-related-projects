package com.djeremy.process.monitor.adapter.startup

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationLoader
import com.djeremy.process.monitor.adapter.properties.ConfigurationPropertiesV2
import com.djeremy.process.monitor.adapter.properties.ProcessConfigurationPropertiesV2
import com.djeremy.process.monitor.adapter.properties.toModelWithSteps
import com.djeremy.process.monitor.adapter.startup.lock.AtomicExecutor
import com.djeremy.process.monitor.domain.process.ProcessConfigurationService
import mu.KotlinLogging.logger
import org.springframework.beans.factory.InitializingBean

open class ConfigurationPropertiesLoaderV2(
        private val configurationProperties: ConfigurationPropertiesV2,
        private val processConfigurationService: ProcessConfigurationService,
        private val startUpLock: AtomicExecutor
) : ProcessConfigurationLoader<ProcessConfigurationPropertiesV2>, InitializingBean {
    private val logger = logger {}

    private val lockName: String = "processConfigurationLoader"

    override fun afterPropertiesSet() {
        logger.info { "Starting loading Process Configuration from Properties" }
        configurationProperties.configurations!!.forEach(::loadFrom)
        logger.info { "Finished loading Process Configuration from Properties" }
    }

    override fun loadFrom(from: ProcessConfigurationPropertiesV2) {
        val model = from.toModelWithSteps()
        logger.info { "Try to load Process Configuration with id ${model.process.id}" }

        startUpLock.execute(lockName) {
            try {
                processConfigurationService.save(model)
                logger.info { "Process Configuration with id ${model.process.id} successfully saved" }
            } catch (ex: AssertionError) {
                logger.info { "Skipping Process Configuration with id ${model.process.id}. The model doesn't meet following criteria: ${ex.localizedMessage}" }
            }
        }
    }
}