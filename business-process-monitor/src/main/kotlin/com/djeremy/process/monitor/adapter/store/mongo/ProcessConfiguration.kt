package com.djeremy.process.monitor.adapter.store.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Duration

const val processConfigurationDaoColName = "process_configuration"

@Document(collection = processConfigurationDaoColName)
data class ProcessConfigurationDao(
        @Id
        val id: String,
        val description: String,
        val duration: Duration
)

interface ProcessConfigurationMongoRepository : MongoRepository<ProcessConfigurationDao, String>