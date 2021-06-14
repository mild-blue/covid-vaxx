package blue.mild.covid.vaxx.jobs

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class JobsRegistrationServiceTest {
    @Test
    fun `service should register all jobs`() {
        val loopMock = mockk<PeriodicExecutorLoop>()
        every { loopMock.schedule(any()) } just Runs

        val jobs = (0..10).map { mockk<PeriodicJob>() }

        JobsRegistrationService(loopMock, jobs).registerAllJobs()

        verifyAll {
            jobs.forEach { job -> loopMock.schedule(job) }
        }
    }

    @Test
    fun `service should not register jobs twice`() {
        val loopMock = mockk<PeriodicExecutorLoop>()
        every { loopMock.schedule(any()) } just Runs

        val jobs = (0..10).map { mockk<PeriodicJob>() }

        val service = JobsRegistrationService(loopMock, jobs)
        assertDoesNotThrow { service.registerAllJobs() }

        verifyAll {
            jobs.forEach { job -> loopMock.schedule(job) }
        }

        assertThrows<IllegalStateException> { service.registerAllJobs() }
    }
}
