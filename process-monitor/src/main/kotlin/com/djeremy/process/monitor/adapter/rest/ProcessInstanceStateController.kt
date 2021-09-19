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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


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
                allConfigurations.getWithCache(it.instance.configurationId),
                allSteps.getWithCache(it.instance.configurationId)
            )
        }
    }

    private fun MutableMap<ProcessConfigurationId, ProcessConfiguration>.getWithCache(
        processConfigurationId: ProcessConfigurationId
    ): ProcessConfiguration {
        return computeIfAbsent(processConfigurationId) { processConfigurationRepository.getBy(it)!! }
    }

    private fun MutableMap<ProcessConfigurationId, List<StepConfigurationModel>>.getWithCache(
        processConfigurationId: ProcessConfigurationId
    ): List<StepConfigurationModel> {
        return computeIfAbsent(processConfigurationId) { stepConfigurationRepository.getById(it) }
    }

}