package com.djeremy.process.monitor.config.adapter

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT15S")
class SchedulerConfigurationV2 {

    @Bean
    fun lockProvider(mongoDatabaseFactory: MongoDatabaseFactory): LockProvider =
            MongoLockProvider(mongoDatabaseFactory.mongoDatabase)
}