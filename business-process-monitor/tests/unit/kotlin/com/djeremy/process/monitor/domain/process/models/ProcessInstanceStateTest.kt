package com.djeremy.process.monitor.domain.process.models

import com.djeremy.process.monitor.domain.`given process instance state in FINISHED stage`
import com.djeremy.process.monitor.domain.`given process instance state in NEW stage`
import com.djeremy.process.monitor.domain.`given step`
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.ADMITTED
import com.djeremy.process.monitor.domain.process.models.ProcessInstanceStages.FINISHED
import com.djeremy.process.monitor.domain.toView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class ProcessInstanceStateTest : Spek({

    describe("test ProcessInstanceState methods") {

        it("test setSteps method") {
            // given
            val instance = `given process instance state in NEW stage`()
            val steps = listOf(`given step`().toView())

            // when
            val result = instance.setSteps(steps)
            // then
            assertSoftly {
                it.assertThat(result.version)
                        .isEqualTo(instance.version)
                it.assertThat(result.steps)
                        .isEqualTo(steps)
                it.assertThat(result)
                        .isNotSameAs(instance)
            }
        }
        it("test finish method") {
            // given
            val instance = `given process instance state in NEW stage`()

            // when
            val result = instance.finish()
            // then
            assertSoftly {
                it.assertThat(result.version)
                        .isEqualTo(instance.version)
                it.assertThat(result.stages)
                        .contains(FINISHED)
                it.assertThat(result)
                        .isNotSameAs(instance)
            }
        }
        it("test admit method") {
            // given
            val instance = `given process instance state in NEW stage`()

            // when
            val result = instance.admit()
            // then
            assertSoftly {
                it.assertThat(result.version)
                        .isEqualTo(instance.version)
                it.assertThat(result.stages)
                        .contains(ADMITTED)
                it.assertThat(result)
                        .isNotSameAs(instance)
            }
        }
        it("test isFinished method when false") {
            // given
            val instance = `given process instance state in NEW stage`()

            // when
            val result = instance.isFinished()
            // then
            assertThat(result).isFalse()
        }
        it("test isFinished method when true") {
            // given
            val instance = `given process instance state in FINISHED stage`()

            // when
            val result = instance.isFinished()
            // then
            assertThat(result).isTrue()
        }
        it("test isAdmitted method when false") {
            // given
            val instance = `given process instance state in FINISHED stage`()

            // when
            val result = instance.isAdmitted()
            // then
            assertThat(result).isFalse()
        }
        it("test isAdmitted method when true") {
            // given
            val instance = `given process instance state in FINISHED stage`().admit()

            // when
            val result = instance.isAdmitted()
            // then
            assertThat(result).isTrue()
        }
    }
})