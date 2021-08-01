package com.djeremy.process.monitor.adapter.startup.lock

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Duration.ofSeconds
import java.time.LocalDateTime.now

private val logger = KotlinLogging.logger {}

interface AtomicExecutor {

    fun execute(atomicProcessId: String, action: () -> Unit)
}

@Transactional(noRollbackFor = [DataIntegrityViolationException::class])
class MongoAtomicExecutorWithLock(
    val repository: StartupLockMongoRepository,
    private val lockDuration: Duration = ofSeconds(10)
) : AtomicExecutor {

    private suspend fun lock(name: String) {
        withTimeoutOrNull(lockDuration.toMillis()) {
            var isNotLocked = true
            var newLock: StartupLockDao? = null

            val doLock: () -> StartupLockDao = {
                val newlyGeneratedLock = StartupLockDao.newInstance(name)
                repository.save(newlyGeneratedLock)
            }

            while (isActive && isNotLocked) {
                try {
                    val currentLock = repository.findByName(name)
                    when {
                        currentLock == null -> {
                            newLock = doLock()
                            isNotLocked = false
                        }
                        now().isAfter(currentLock.acquired.plus(lockDuration)) -> {
                            repository.deleteById(currentLock.id)
                            newLock = doLock()
                            isNotLocked = false
                        }
                        else -> {
                            delay(100)
                        }
                    }
                } catch (e: DataIntegrityViolationException) {
                    logger.warn("Race condition has occurred. Try to acquire lock again.")
                }
            }
            newLock
        } ?: throw RuntimeException("Cannot acquire startup lock for $name")
    }

    private fun unlock(name: String) {
        repository.findByName(name)?.let { repository.delete(it) }
    }

    override fun execute(atomicProcessId: String, action: () -> Unit) = runBlocking {
        lock(atomicProcessId)
        try {
            action()
        } finally {
            withContext(NonCancellable) {
                unlock(atomicProcessId)
            }
        }
    }
}