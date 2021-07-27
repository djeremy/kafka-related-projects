package com.djeremy.process.monitor.adapter.startup.lock

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StartupLockConfiguration {

    @Bean
    fun startupLock(mongoRepository: StartupLockMongoRepository): AtomicExecutor = MongoAtomicExecutorWithLock(mongoRepository)
}