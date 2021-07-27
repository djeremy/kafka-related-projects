package com.djeremy.process.monitor.embeded.v2.kafka

import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.task.ProcessExpiredAlert
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@TestConfiguration
class TestConfig {

    @Bean(name = ["testAlert"])
    @Profile(value = ["test"])
    fun processExpiredAlert(): ProcessExpiredAlert = MockLoggingProcessExpiredAlert()

}

class MockLoggingProcessExpiredAlert: ProcessExpiredAlert{

    var alerted: List<ProcessInstanceState> = emptyList()

    override fun alertOn(processes: List<ProcessInstanceState>) {
        alerted = processes
    }

}