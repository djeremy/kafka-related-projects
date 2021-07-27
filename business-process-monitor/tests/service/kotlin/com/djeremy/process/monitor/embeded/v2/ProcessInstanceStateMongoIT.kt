package com.djeremy.process.monitor.embeded.v2

import com.djeremy.process.monitor.adapter.store.ProcessInstanceStateMongoAdapter
import com.djeremy.process.monitor.adapter.store.StepConfigurationMongoAdapter
import com.djeremy.process.monitor.adapter.store.StepMongoAdapter
import com.djeremy.process.monitor.adapter.store.mongo.ProcessInstanceStateMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.StepMongoRepository
import com.djeremy.process.monitor.domain.`given process instance state in ADMITTED stage`
import com.djeremy.process.monitor.domain.`given process instance state in FINISHED stage`
import com.djeremy.process.monitor.domain.`given process instance state in NEW stage`
import com.djeremy.process.monitor.domain.`given step from`
import com.djeremy.process.monitor.domain.`given step`
import com.djeremy.process.monitor.domain.port.store.ProcessInstanceStateRepository
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import com.djeremy.process.monitor.domain.process.DefaultProcessInstanceStateService
import com.djeremy.process.monitor.domain.process.DefaultStepService
import com.djeremy.process.monitor.domain.process.ProcessInstanceStateService
import com.djeremy.process.monitor.domain.process.StepService
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceState
import com.djeremy.process.monitor.domain.process.models.Step
import com.djeremy.process.monitor.domain.task.DefaultProcessInstanceStateTask
import com.djeremy.process.monitor.domain.task.ProcessInstanceStateTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime.now

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
class ProcessInstanceStateMongoIT {

    @Autowired
    lateinit var stepMongoRepository: StepMongoRepository

    @Autowired
    lateinit var processInstanceStateMongoRepository: ProcessInstanceStateMongoRepository

    @Autowired
    lateinit var stepConfigurationMongoRepository: StepConfigurationMongoRepository


    lateinit var stepRepository: StepRepository
    lateinit var stepConfigurationRepository: StepConfigurationRepository
    lateinit var processInstanceStateRepository: ProcessInstanceStateRepository

    lateinit var stepService: StepService
    lateinit var processInstanceStateService: ProcessInstanceStateService

    lateinit var processInstanceStateTask: ProcessInstanceStateTask

    @BeforeAll
    fun init() {
        stepRepository = StepMongoAdapter(stepMongoRepository)
        processInstanceStateRepository = ProcessInstanceStateMongoAdapter(processInstanceStateMongoRepository)
        stepConfigurationRepository = StepConfigurationMongoAdapter(stepConfigurationMongoRepository)

        stepService = DefaultStepService(stepRepository, stepConfigurationRepository)
        processInstanceStateService = DefaultProcessInstanceStateService(processInstanceStateRepository)
        processInstanceStateTask = DefaultProcessInstanceStateTask(processInstanceStateService, stepService)
    }

    @BeforeEach
    fun beforeEach() {
        stepMongoRepository.deleteAll()
        processInstanceStateMongoRepository.deleteAll()
    }

    @Test
    internal fun `test ProcessInstanceStateTask should aggregate all steps correctly`() {
        // given
        // steps with process instance one
        val step1_1 = `given step`(withProcessInstanceId = true).save()
        val step1_2 = `given step from`(step1_1).save()
        val step1_3 = `given step from`(step1_1, isLast = true).save()

        // steps with process instance second
        val step2_1 = `given step`(withProcessInstanceId = true).save()
        val step2_2 = `given step from`(step2_1, isLast = false).save()

        // steps without process instances
        val step3_1 = `given step`().save()
        val step4_1 = `given step`().save()

        // when
        processInstanceStateTask.execute()

        // then
        val instances = processInstanceStateMongoRepository.findAll()
        val steps = stepMongoRepository.findAll()

        assertThat(instances.map { stateDao -> stateDao.instance!!.id to stateDao.steps!!.map { it.id } })
                .describedAs("Instances stats has correct steps assigned")
                .hasSize(2)
                .containsOnly(
                        step1_1.processInstanceId!!.value to listOf(step1_1.id, step1_2.id, step1_3.id),
                        step2_2.processInstanceId!!.value to listOf(step2_1.id, step2_2.id))

        assertThat(steps.map { it.id to it.isNewlyInstanceAssigned })
                .hasSize(7)
                .containsOnly(
                        step1_1.id to false,
                        step1_2.id to false,
                        step1_3.id to false,
                        step2_1.id to false,
                        step2_2.id to false,
                        step3_1.id to null,
                        step4_1.id to null
                )
    }

    @Test
    internal fun `test getNotAdmittedIdsBefore should return only not admitted states`() {
        // given
        val processInstanceStateNew = `given process instance state in NEW stage`().save()
        val processInstanceStateFinished = `given process instance state in FINISHED stage`().save()
        `given process instance state in ADMITTED stage`().save()

        // when
        val result = processInstanceStateRepository.getNotAdmittedIdsBefore(now().plusSeconds(2))

        // then
        assertThat(result).hasSize(2)
        assertThat(result.map{it.processInstance})
                .containsOnly(processInstanceStateNew.instance, processInstanceStateFinished.instance)
    }

    private fun ProcessInstanceState.save(): ProcessInstanceState = apply { processInstanceStateRepository.save(this) }

    private fun Step.save(): Step = apply { stepRepository.save(this) }
}