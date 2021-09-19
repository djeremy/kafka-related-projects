package com.djeremy.process.monitor.rest

import com.djeremy.process.monitor.adapter.rest.ProcessInstanceStateController
import com.djeremy.process.monitor.domain.`given process configuration`
import com.djeremy.process.monitor.domain.`given process instance state in NEW stage`
import com.djeremy.process.monitor.domain.`given single step configuration`
import com.djeremy.process.monitor.domain.port.store.ProcessConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.process.models.ProcessConfigurationId
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceId
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.hasItems
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.isEqualTo
import java.time.Duration
import java.util.*

@WebMvcTest(
    controllers = [ProcessInstanceStateController::class]
)
class ProcessInstanceStateControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var processInstanceStateRepository: ProcessInstanceStateRepository

    @Autowired
    lateinit var processConfigurationRepository: ProcessConfigurationRepository

    @Autowired
    lateinit var stepConfigurationRepository: StepConfigurationRepository

    @BeforeEach
    fun setUp() {
        clearMocks(
            processConfigurationRepository,
            processConfigurationRepository,
            stepConfigurationRepository
        )
    }

    @TestConfiguration
    class ProcessInstanceStateControllerTestConfiguration {

        @Bean
        fun processInstanceStateRepository(): ProcessInstanceStateRepository = mockk()

        @Bean
        fun processConfigurationRepository(): ProcessConfigurationRepository = mockk()

        @Bean
        fun stepConfigurationRepository(): StepConfigurationRepository = mockk()

    }

    @Test
    fun testMe() {
        // given
        val configurationId = ProcessConfigurationId(UUID.randomUUID().toString())

        val pageSize = 10
        val page = 0
        val processInstanceId = ProcessInstanceId(UUID.randomUUID().toString())
        val processInstance = `given process instance state in NEW stage`(processInstanceId, configurationId.value)
        val processConfiguration = `given process configuration`(
            duration = Duration.ZERO,
            configurationId = processInstance.instance.configurationId.value
        )
        val stepConfiguration_1 = `given single step configuration`(
            stepId = processInstance.steps.first().stepId.value
        )
        val stepConfiguration_2 = `given single step configuration`()
        val stepConfigurations = listOf(stepConfiguration_1, stepConfiguration_2)

        // given repository mocked
        every { processInstanceStateRepository.getBy(configurationId, any()) } returns PageImpl(listOf(processInstance))
        every { processConfigurationRepository.getBy(configurationId) } returns processConfiguration
        every { stepConfigurationRepository.getById(configurationId) } returns stepConfigurations

        // when
        mockMvc.get("/api/process-instances") {
            param("configurationId", configurationId.value)
            param("page", page.toString())
            param("size", pageSize.toString())
            header(ACCEPT, "application/vnd.process-instances-v1+json")
        }
            // then
            .andDo { print() }
            .andExpect { header { string(CONTENT_TYPE, "application/vnd.process-instances-v1+json") } }
            .andExpect { status { isEqualTo(200) } }
            .andExpect { jsonPath("$.content", hasSize<Any>(1)) }
            .andExpect { jsonPath("$.content[*].processInstanceId", hasItems(processInstanceId.value)) }
            .andExpect { jsonPath("$.content[*].processConfiguration.id", hasItems(processConfiguration.id.value)) }
            .andExpect {
                jsonPath(
                    "$.content[*].processConfiguration.description",
                    hasItems(processConfiguration.description)
                )
            }.andExpect {
                jsonPath(
                    "$.content[*].steps[*].stepDescription",
                    hasItems(stepConfiguration_1.getDescription())
                )
            }
    }
}