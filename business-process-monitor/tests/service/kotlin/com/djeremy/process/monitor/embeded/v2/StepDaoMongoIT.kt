package com.djeremy.process.monitor.embeded.v2

import com.djeremy.process.monitor.adapter.store.StepConfigurationMongoAdapter
import com.djeremy.process.monitor.adapter.store.StepMongoAdapter
import com.djeremy.process.monitor.adapter.store.mongo.MultiExclusiveStepConfigurationDaoV2
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationDaoV2
import com.djeremy.process.monitor.adapter.store.mongo.StepConfigurationMongoRepository
import com.djeremy.process.monitor.adapter.store.mongo.StepMongoRepository
import com.djeremy.process.monitor.adapter.store.toDao
import com.djeremy.process.monitor.domain.`given multiple exclusive step configuration`
import com.djeremy.process.monitor.domain.`given single step configuration`
import com.djeremy.process.monitor.domain.`given step from`
import com.djeremy.process.monitor.domain.`given step`
import com.djeremy.process.monitor.domain.port.store.StepConfigurationRepository
import com.djeremy.process.monitor.domain.port.store.StepRepository
import com.djeremy.process.monitor.domain.process.models.Step
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
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
class StepDaoMongoIT {

    @Autowired
    lateinit var stepMongoRepository: StepMongoRepository

    @Autowired
    lateinit var stepConfigurationMongoRepository: StepConfigurationMongoRepository
    lateinit var stepRepository: StepRepository
    lateinit var stepConfigurationRepository: StepConfigurationRepository

    @BeforeAll
    fun init() {
        stepRepository = StepMongoAdapter(stepMongoRepository)
        stepConfigurationRepository = StepConfigurationMongoAdapter(stepConfigurationMongoRepository)
        `populate table with random steps`()
    }

    @Test
    internal fun `test getWithoutProcessInstanceBy and getWithProcessInstanceBy when steps are saved WITHOT process instance id`() {
        // given
        val step1 = `given step`().save()
        `given step from`(step1).save()
        `given step from`(step1).save()
        // when
        val resultStepsNoProcess = stepRepository.getWithoutProcessInstanceBy(step1.configurationId.value, step1.eventId)
        val resultStepsWithProcess = stepRepository.getWithProcessInstanceBy(step1.configurationId.value, step1.eventId)
        // then
        assertThat(resultStepsNoProcess).hasSize(2)
        assertThat(resultStepsWithProcess).hasSize(0)
    }

    @Test
    internal fun `test getWithoutProcessInstanceBy and getWithProcessInstanceBy when steps are saved WITH process instance id`() {
        // given
        val step1 = `given step`(withProcessInstanceId = true).save()
        `given step from`(step1).save()
        `given step from`(step1).save()
        // when
        val resultStepsNoProcess = stepRepository.getWithoutProcessInstanceBy(step1.configurationId.value, step1.eventId)
        val resultStepsWithProcess = stepRepository.getWithProcessInstanceBy(step1.configurationId.value, step1.eventId)
        // then
        assertThat(resultStepsNoProcess).hasSize(0)
        assertThat(resultStepsWithProcess).hasSize(2)
    }

    @Test
    internal fun `test getWithoutProcessInstanceIdBy (by eventIds)`() {
        // given
        val step1 = `given step`().save()
        val step2 = `given step`(withProcessInstanceId = true).save()
        // when
        val resultStepsNoProcess = stepRepository.getWithoutProcessInstanceBy(step1.configurationId.value, listOf(step1.eventId))
        val resultStepsWithProcess = stepRepository.getWithoutProcessInstanceBy(step1.configurationId.value, listOf(step2.eventId))
        // then
        assertThat(resultStepsNoProcess).hasSize(1)
        assertThat(resultStepsWithProcess).hasSize(0)
    }

    @Test
    internal fun `test getNewSteps should return sorted list of newly steps`() {
        // given
        val step1 = `given step`(
                receivedAt = now().minusHours(5),
                withProcessInstanceId = true,
                isNewlyAssigned = true)
                .save()
        val step2 = `given step`(
                receivedAt = now().minusHours(10),
                withProcessInstanceId = true,
                isNewlyAssigned = true)
                .save()
        // when
        val newlyAssignedSteps = stepRepository.getNewSteps()
        // then
        // step2 should be returned before step1
        assertThat(newlyAssignedSteps.take(2))
                .usingElementComparatorIgnoringFields("receivedAt")
                .containsExactly(step2, step1)
    }

    @Test
    fun `test saveAll configurations with different type of steps`() {
        // given
        val configurationId = "configurationId"
        val stepConfiguration1 = `given single step configuration`(configurationId)
        val stepConfiguration2 = `given multiple exclusive step configuration`(configurationId)

        stepConfigurationRepository.saveAll(listOf(stepConfiguration1, stepConfiguration2))
        // when
        val savedConfigurations = stepConfigurationMongoRepository.findAll()
        // then
        assertThat(savedConfigurations).hasSize(2)
                .usingElementComparatorIgnoringFields("id")
                .containsAll(listOf(stepConfiguration1.toDao(), stepConfiguration2.toDao()))

        assertThat(savedConfigurations.map { it::class })
                .containsAll(listOf(StepConfigurationDaoV2::class, MultiExclusiveStepConfigurationDaoV2::class))
    }

    private fun `populate table with random steps`() = (0 until 20).forEach { _ -> `given step`().save() }
    private fun Step.save(): Step = apply { stepRepository.save(this) }
}