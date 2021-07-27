package com.djeremy.process.monitor.domain.task

import com.djeremy.process.monitor.domain.process.ProcessInstanceStateService
import com.djeremy.process.monitor.domain.process.StepService
import com.djeremy.process.monitor.domain.process.models.Step
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifySequence
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class DefaultProcessInstanceStateTaskTest : Spek({

    describe("Test DefaultProcessInstanceStateTask") {
        val service = mockk<ProcessInstanceStateService>()
        val stepService = mockk<StepService>()

        val task: ProcessInstanceStateTask = DefaultProcessInstanceStateTask(service, stepService)

        afterEachTest { clearMocks(service, stepService) }

        it("should call all services"){
            // given
            val newlySteps :List<Step> = mockk()
            every {  stepService.getNewlyAssignedSteps()} returns newlySteps
            every { service.aggregate(any<List<Step>>()) } just runs
            every { stepService.admitAssignedSteps(any()) } just runs

            // when
            task.execute()

            // then
            verifySequence {
                stepService.getNewlyAssignedSteps()
                service.aggregate(newlySteps)
                stepService.admitAssignedSteps(newlySteps)
            }
        }
    }
})