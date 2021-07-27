package com.djeremy.process.monitor.adapter.task

import com.djeremy.process.monitor.domain.task.ProcessInstanceExpiredTask
import mu.KotlinLogging.logger
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

private val logger = logger {}

@Component
@Profile(value = ["!test"])
class ProcessInstanceExpiredTaskScheduler(
        private val task: ProcessInstanceExpiredTask) {

    @Scheduled(cron = "* */10 * * * *")
    @SchedulerLock(name = "ProcessInstanceExpiredTaskScheduler##schedule", lockAtLeastFor = "PT5M", lockAtMostFor = "PT10M")
    fun schedule() {
        measureTimeMillis {
            logger.info { "[${ProcessInstanceExpiredTask::class.simpleName}] started" }
            getTask().execute()
        }.let { logger.info { "[${ProcessInstanceExpiredTask::class.simpleName}] finished in [$it] millis" } }
    }

    fun getTask(): ProcessInstanceExpiredTask = task

}