package com.djeremy.process.monitor.adapter.task

import com.djeremy.process.monitor.domain.task.ProcessInstanceStateTask
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class ProcessInstanceStateTaskSchedulerTest : Spek({

    describe("Test ProcessInstanceStateTaskScheduler") {
        it("should call task execute method") {
            // given
            val task = mockk<ProcessInstanceStateTask>()
            val scheduler = ProcessInstanceStateTaskScheduler(task)

            every { task.execute() } just runs
            // when
            scheduler.schedule()

            // then
            verify { task.execute() }
        }
    }

})