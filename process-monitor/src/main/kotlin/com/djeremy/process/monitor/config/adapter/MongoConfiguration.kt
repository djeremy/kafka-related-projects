package com.djeremy.process.monitor.config.adapter

import com.mongodb.WriteConcern.ACKNOWLEDGED
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.WriteConcernResolver

@Configuration
class MongoConfiguration {

    @Bean
    fun writeConcernResolver(): WriteConcernResolver {
        return WriteConcernResolver { ACKNOWLEDGED }
    }
}