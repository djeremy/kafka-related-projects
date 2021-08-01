package com.djeremy.process.monitor.adapter.task

import com.djeremy.process.monitor.domain.task.ProcessInstanceExpiredTask
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal class ProcessInstanceExpiredTaskSchedulerTest : Spek({
    describe("Test ProcessInstanceExpiredTaskScheduler") {
        it("should call task execute method") {
            // given
            val task = mockk<ProcessInstanceExpiredTask>()
            val scheduler = ProcessInstanceExpiredTaskScheduler(task)

            every { task.execute() } just runs
            // when
            scheduler.schedule()

            // then
            verify { task.execute() }
        }
    }

})