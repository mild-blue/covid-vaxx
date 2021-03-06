package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogging
import pw.forst.tools.katlib.InstantTimeProvider
import java.time.Instant

class TimeLoadTest(
    targetHost: String,
    credentials: LoginDtoIn,
    requestTimeoutsSeconds: Int = 60,
    private val coroutineWorkers: Int = 2,
    private val runningTimeInSeconds: Long
) : LoadTest(targetHost, credentials, requestTimeoutsSeconds) {

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
                        runPatientRegistrationWithBuilder(
                            { answers, insuranceCompany, confirmation ->
                                PatientRegistrationDtoIn(
                                    firstName = "John",
                                    lastName = "Doe",
                                    personalNumber = "7401040020",
                                    phoneNumber = "+420123456789",
                                    email = "john@doe.com",
                                    insuranceCompany, answers, confirmation
                                )
                            },
                            { logger.info { "Done: $it - time left ${(endTime.epochSecond - now.epochSecond)}s" } }
                        )
                    }
                }
            }
        }

        return callsCollection.toList()
    }

}
