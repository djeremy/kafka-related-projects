package com.djeremy.process.monitor.domain.port.store

import com.djeremy.process.monitor.domain.process.models.ProcessInstance
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStateProjection
import java.time.LocalDateTime

interface ProcessInstanceStateRepository {
    fun getBy(id: ProcessInstance): ProcessInstanceState?
    fun getNotAdmittedIdsBefore(before: LocalDateTime): List<ProcessInstanceStateProjection>
    fun getBy(ids: List<ProcessInstance>): List<ProcessInstanceState>
    fun save(processInstanceState: ProcessInstanceState)
    fun saveAll(processInstanceStates: List<ProcessInstanceState>)
}