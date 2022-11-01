package blue.mild.covid.vaxx.integrations.api

import blue.mild.covid.vaxx.integrations.AnswerDtoInForTest
import blue.mild.covid.vaxx.integrations.ConfirmationDtoInForTest
import blue.mild.covid.vaxx.integrations.RegistrationApiTestHelper
import blue.mild.covid.vaxx.integrations.WrongObjectDtoInForTest
import blue.mild.covid.vaxx.integrations.platform.RequestMetric
import blue.mild.covid.vaxx.integrations.platform.defaultPatientRegistrationBuilder
import io.ktor.http.HttpStatusCode
import mu.KLogging
import java.util.UUID

open class RegistrationApiTest(
    targetHost: String
) : RegistrationApiTestHelper(targetHost, 10) {

    private companion object : KLogging()

    @Suppress("LongMethod") // fine here
    override suspend fun execute(): List<RequestMetric> {

        runPatientRegistrationWithBuilder {}
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                lastName = ""
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) {}
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                firstName = null
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                phoneNumber = "12"
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                phoneNumber = "---"
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                email = "a@a.cz"
            )
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                personalNumber = "33"
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                personalNumber = "karel"
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeAnswers = listOf(AnswerDtoInForTest(UUID.randomUUID(), true)),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeAnswers = listOf("a"),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeAnswers = listOf(AnswerDtoInForTest(UUID.randomUUID(), "a")),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeAnswers = listOf(AnswerDtoInForTest("a", "a")),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeAnswers = "a",
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeInsuranceCompany = "a",
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                confirmation = "a"
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                confirmation = ConfirmationDtoInForTest("a")
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                confirmation = WrongObjectDtoInForTest("a")
            ),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        return callsCollection.toList()
    }
}
