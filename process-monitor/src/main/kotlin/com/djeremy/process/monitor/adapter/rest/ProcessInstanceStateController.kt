package com.djeremy.process.monitor.adapter.rest

import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfiguration
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.StepConfigurationModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
@RequestMapping(path = ["/api"])
class ProcessInstanceStateController(
    var processInstanceStateRepository: ProcessInstanceStateRepository,
    var processConfigurationRepository: ProcessConfigurationRepository,
    var stepConfigurationRepository: StepConfigurationRepository
) {

    @GetMapping(path = ["/process-instances"], produces = ["application/vnd.process-instances-v1+json"])
    fun getProcessInstances(
        @RequestParam("configurationId") configurationId: String,
        @PageableDefault pageable: Pageable
    ): Page<ProcessInstanceStateDto> {
        return kotlin.runCatching {
            processInstanceStateRepository.getBy(ProcessConfigurationId(configurationId), pageable)
                .run { transform(this) }
        }.getOrThrow()
    }


    private fun transform(processInstances: Page<ProcessInstanceState>): Page<ProcessInstanceStateDto> {
        val allConfigurations: MutableMap<ProcessConfigurationId, ProcessConfiguration> = mutableMapOf()
        val allSteps: MutableMap<ProcessConfigurationId, List<StepConfigurationModel>> = mutableMapOf()

        return processInstances.map {
            mapFrom(
                it,
                allConfigurations.getOrCache(it.instance.configurationId),
                allSteps.getOrCache(it.instance.configurationId)
            )
        }
    }

    private fun MutableMap<ProcessConfigurationId, ProcessConfiguration>.getOrCache(
        processConfigurationId: ProcessConfigurationId
    ): ProcessConfiguration =
        computeIfAbsent(processConfigurationId) { processConfigurationRepository.getBy(it)!! }

    private fun MutableMap<ProcessConfigurationId, List<StepConfigurationModel>>.getOrCache(
        processConfigurationId: ProcessConfigurationId
    ): List<StepConfigurationModel> =
        computeIfAbsent(processConfigurationId) { stepConfigurationRepository.getById(it) }

}