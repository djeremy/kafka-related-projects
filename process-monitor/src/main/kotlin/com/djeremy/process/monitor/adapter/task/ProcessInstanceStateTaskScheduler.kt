package com.djeremy.process.monitor.adapter.task

import com.djeremy.process.monitor.domain.task.ProcessInstanceStateTask
import mu.KotlinLogging.logger
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

private val logger = logger {}

@Component
@Profile(value = ["!test"])
class ProcessInstanceStateTaskScheduler(
        private val task: ProcessInstanceStateTask) {

    @Scheduled(cron = "*/15 * * * * *")
    @SchedulerLock(name = "ProcessInstanceStateTaskScheduler##schedule", lockAtMostFor = "30S")
    fun schedule() {
        measureTimeMillis {
            logger.info { "[${ProcessInstanceStateTask::class.simpleName}] started" }
            getTask().execute()
        }.let { logger.info { "[${ProcessInstanceStateTask::class.simpleName}] finished in [$it] millis" } }
    }

    fun getTask(): ProcessInstanceStateTask = task
}