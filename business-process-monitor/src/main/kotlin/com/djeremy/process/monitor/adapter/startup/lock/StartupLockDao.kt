package com.djeremy.process.monitor.adapter.startup.lock

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime
import java.util.UUID

@Document
class StartupLockDao(
        @Id
        val id: UUID = UUID.randomUUID(),
        @Indexed(unique = true)
        val name: String,
        val acquired: LocalDateTime
) {
    companion object {
        fun newInstance(name: String) = StartupLockDao(name = name, acquired = LocalDateTime.now())
    }
}

interface StartupLockMongoRepository : MongoRepository<StartupLockDao, UUID> {

    fun findByName(name: String): StartupLockDao?
}