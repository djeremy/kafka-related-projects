package com.djeremy.process.monitor.domain.port.store

import com.djeremy.process.monitor.domain.process.models.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface ProcessInstanceStateRepository {
    fun getBy(id: ProcessInstance): ProcessInstanceState?
    fun getNotAdmittedIdsBefore(before: LocalDateTime): List<ProcessInstanceStateProjection>
    fun getBy(ids: List<ProcessInstance>): List<ProcessInstanceState>
    fun getBy(processConfigurationId: ProcessConfigurationId, pageable: Pageable): Page<ProcessInstanceState>

    fun save(processInstanceState: ProcessInstanceState)
    fun saveAll(processInstanceStates: List<ProcessInstanceState>)
}