package blue.mild.covid.vaxx.security.ddos

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pw.forst.katlib.TimeProvider
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LinearRateLimiterTest {

    private fun timeProvider(mockNow: Instant) = mockk<TimeProvider<Instant>> {
        every { now() } returns mockNow
    }

    @Test
    fun `test hit the request rate, then reset and then hit again`() {
        val limit = 10L
        val resetTime = Duration.ofMinutes(10)
        val now = Instant.now()
        val timeProvider = timeProvider(now)
        val diffSeconds = resetTime.seconds

        val hostName = "hostUnderTheTest"

        val instance = LinearRateLimiter(
            limit = limit,
            resetTime = resetTime,
            nowProvider = timeProvider
        )

        val workers = 5
        val repetitionsPerWorker = 10
        // just to check the parameters, that we can really get denied result
        assertTrue { limit < (workers + repetitionsPerWorker) }
        instance.stressTest(
            workers = workers,
            repetitionsPerWorker = repetitionsPerWorker,
            hostName = hostName,
            limit = limit,
            diffSeconds = diffSeconds
        )
        // verify that the host is still locked out
        assertEquals(diffSeconds, instance.processRequest(hostName))
        // trigger the reset
        val newNow = now.plus(resetTime.plusMinutes(1))
        every { timeProvider.now() } returns newNow
        // verify that the host is now free to run the requests again
        instance.stressTest(
            workers = workers,
            repetitionsPerWorker = repetitionsPerWorker,
            hostName = hostName,
            limit = limit,
            diffSeconds = diffSeconds
        )
        // verify that the host is now locked out again
        assertEquals(diffSeconds, instance.processRequest(hostName))
    }


    private fun LinearRateLimiter.stressTest(
        workers: Int, repetitionsPerWorker: Int, hostName: String, limit: Long, diffSeconds: Long
    ) {
        val limiter = this
        val counter = AtomicInteger(0)
        // execute test that will lock our host down
        runBlocking {
            repeat(workers / 2) {
                // measured host
                launch {
                    repeat(repetitionsPerWorker) {
                        val nextTime = counter.incrementAndGet()
                        val result = limiter.processRequest(hostName)
                        if (nextTime >= limit) {
                            assertEquals(diffSeconds, result)
                        } else {
                            assertNull(result)
                        }
                    }
                }
                // random hosts must have an access
                launch {
                    repeat(repetitionsPerWorker) {
                        assertNull(limiter.processRequest(UUID.randomUUID().toString()))
                    }
                }
            }
        }
    }

}
