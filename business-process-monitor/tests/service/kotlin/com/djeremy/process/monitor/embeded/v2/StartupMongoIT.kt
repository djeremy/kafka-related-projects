package com.djeremy.process.monitor.embeded.v2

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationLoader
import com.djeremy.process.monitor.adapter.properties.ConfigurationPropertiesV2
import com.djeremy.process.monitor.adapter.properties.ProcessConfigurationPropertiesV2
import com.djeremy.process.monitor.adapter.properties.`given random properties with given number of steps`
import com.djeremy.process.monitor.adapter.startup.ConfigurationPropertiesLoaderV2
import com.djeremy.process.monitor.adapter.startup.lock.AtomicExecutor
import com.djeremy.process.monitor.adapter.startup.lock.StartupLockConfiguration
import com.djeremy.process.monitor.adapter.store.ProcessConfigurationMongoAdapter
import com.djeremy.process.monitor.adapter.store.StepConfigurationMongoAdapter
import com.djeremy.process.monitor.adapter.store.mongo.ProcessConfigurationMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationMongoRepository
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.DefaultProcessConfigurationService
import com.djeremy.process.monitor.domain.process.DefaultStepConfigurationService
import com.djeremy.process.monitor.domain.process.ProcessConfigurationService
import com.djeremy.process.monitor.domain.process.StepConfigurationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@Import(StartupLockConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
class StartupMongoIT {

    @Autowired
    lateinit var startupLock: AtomicExecutor

    @Autowired
    lateinit var stepConfigurationMongoRepository: StepConfigurationMongoRepository

    @Autowired
    lateinit var processConfigurationMongoRepository: ProcessConfigurationMongoRepository

    private lateinit var stepConfigurationRepository: StepConfigurationRepository
    private lateinit var processConfigurationRepository: ProcessConfigurationRepository

    private lateinit var stepConfigurationService: StepConfigurationService
    private lateinit var processConfigurationService: ProcessConfigurationService


    lateinit var processConfigurationLoaderFun: (properties: ProcessConfigurationPropertiesV2) -> ProcessConfigurationLoader<ProcessConfigurationPropertiesV2>

    @BeforeAll
    fun init() {
        stepConfigurationRepository = StepConfigurationMongoAdapter(stepConfigurationMongoRepository)
        stepConfigurationService = DefaultStepConfigurationService(stepConfigurationRepository)

        processConfigurationRepository = ProcessConfigurationMongoAdapter(processConfigurationMongoRepository)

        processConfigurationService = DefaultProcessConfigurationService(processConfigurationRepository, stepConfigurationService)

        processConfigurationLoaderFun = {
            val configuration = ConfigurationPropertiesV2().apply { configurations = listOf(it) }
            ConfigurationPropertiesLoaderV2(configuration, processConfigurationService, startupLock)
        }
    }

    @Test
    internal fun `test processConfigurationLoader simulating multi instance scenario`() {
        // given
        val numberOfSteps = 2
        val properties = `given random properties with given number of steps`(numberOfSteps)

        // when
        runBlocking {
            val loaderTasks = mutableListOf<Deferred<Unit>>()
            repeat(50) {
                loaderTasks.add(async {
                    val loader = processConfigurationLoaderFun(properties)
                    loader.loadFrom(properties)
                })
            }

            awaitAll(*loaderTasks.toTypedArray())
        }

        // then
        assertThat(stepConfigurationMongoRepository.count())
                .isEqualTo(numberOfSteps.toLong())
    }
}