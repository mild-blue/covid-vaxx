package blue.mild.covid.vaxx.api

import blue.mild.covid.vaxx.platform.RequestMetric
import blue.mild.covid.vaxx.platform.defaultPatientRegistrationBuilder
import blue.mild.covid.vaxx.utils.AnswerDtoInForTest
import blue.mild.covid.vaxx.utils.ConfirmationDtoInForTest
import blue.mild.covid.vaxx.utils.RegistrationApiTestHelper
import blue.mild.covid.vaxx.utils.WrongObjectDtoInForTest
import io.ktor.http.HttpStatusCode
import mu.KLogging
import java.util.UUID

open class RegistrationApiTest(
    targetHost: String
) : RegistrationApiTestHelper(targetHost, 10) {

    private companion object : KLogging()

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
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = listOf("a"),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = listOf(AnswerDtoInForTest(UUID.randomUUID(), "a")),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = listOf(AnswerDtoInForTest("a", "a")),
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = "a",
            httpStatus = HttpStatusCode.BadRequest
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
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
