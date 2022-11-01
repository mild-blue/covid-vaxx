package blue.mild.covid.vaxx.integrations.platform

import blue.mild.covid.vaxx.integrations.RegistrationApiTestHelper
import dev.forst.katlib.InstantTimeProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogging
import java.time.Instant

class TimeLoadTest(
    targetHost: String,
    requestTimeoutsSeconds: Int = 60,
    private val coroutineWorkers: Int = 2,
    private val runningTimeInSeconds: Long
) : RegistrationApiTestHelper(targetHost, requestTimeoutsSeconds) {

    private companion object : KLogging()

    private val now
        get() = InstantTimeProvider.now()

    private fun isTimeLeft(endTime: Instant) = endTime.isAfter(now)

    override suspend fun execute(): List<RequestMetric> {
        val endTime = now.plusSeconds(runningTimeInSeconds)

        coroutineScope {
            repeat(coroutineWorkers) {
                launch {
                    while (isTimeLeft(endTime)) {
                        runPatientRegistrationWithBuilder(defaultPatientRegistrationBuilder()) {
                            logger.info { "Done: $it - time left ${(endTime.epochSecond - now.epochSecond)}s" }
                        }
                    }
                }
            }
        }
        return callsCollection.toList()
    }
}
