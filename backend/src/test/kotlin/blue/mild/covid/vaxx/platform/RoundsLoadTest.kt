package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogging

class RoundsLoadTest(
    targetHost: String,
    credentials: LoginDtoIn,
    requestTimeoutsSeconds: Int = 60,
    private val coroutineWorkers: Int = 2,
    private val rounds: Int
) : LoadTest(targetHost, credentials, requestTimeoutsSeconds) {

    private companion object : KLogging()

    private val singleWorkerRounds = rounds / coroutineWorkers

    override suspend fun execute(): List<RequestMetric> {
        coroutineScope {
            repeat(coroutineWorkers) {
                launch {
                    repeat(singleWorkerRounds) {
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
                            { logger.info { "Done: $it/$rounds" } }
                        )
                    }
                }
            }
        }

        return callsCollection.toList()
    }

}
