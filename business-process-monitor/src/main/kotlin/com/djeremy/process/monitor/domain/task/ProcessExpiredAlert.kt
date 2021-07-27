package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.StepView
import mu.KotlinLogging.logger

interface ProcessExpiredAlert {
    fun alertOn(processes: List<ProcessInstanceState>)
}

class LoggingProcessExpiredAlert : ProcessExpiredAlert {

    val logger = logger {}

    override fun alertOn(processes: List<ProcessInstanceState>) {
        val stepsToString: (List<StepView>) -> String = { it.joinToString(separator = "\n", prefix = "   ") }
        processes.forEach {
            if (it.isFinished()) {
                logger.warn {
                    "[Process configuration::${it.instance.configurationId.value}] -> Process finished but exceeded expected time window. ProcessInstanceId [${it.instance.id}]. Please review steps manually:" +
                            "\n${stepsToString(it.steps)}"
                }
            } else {
                logger.error {
                    "[Process configuration::${it.instance.configurationId.value}] -> Process has not finished in expected time window. ProcessInstanceId [${it.instance.id}]. Please review steps manually:" +
                            "\n${stepsToString(it.steps)}"
                }
            }
        }
    }
}