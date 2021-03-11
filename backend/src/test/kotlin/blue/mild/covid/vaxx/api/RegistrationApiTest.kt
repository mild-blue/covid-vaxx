package blue.mild.covid.vaxx.api

import blue.mild.covid.vaxx.utils.AnswerDtoInForTest
import blue.mild.covid.vaxx.utils.ConfirmationDtoInForTest
import blue.mild.covid.vaxx.platform.RequestMetric
import blue.mild.covid.vaxx.utils.WrongObjectDtoInForTest
import blue.mild.covid.vaxx.platform.defaultPatientRegistrationBuilder
import blue.mild.covid.vaxx.utils.RegistrationApiTestHelper
import mu.KLogging
import org.apache.http.HttpStatus
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
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) {}
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                firstName = null
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                phoneNumber = "12"
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                phoneNumber = "---"
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                email = "a@a.cz"
            ),
            httpStatus = HttpStatus.SC_OK
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                personalNumber = "33"
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }
        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                personalNumber = "karel"
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(),
            maybeAnswers = listOf(AnswerDtoInForTest(UUID.randomUUID(), true)),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

//        TODO All the tests below are not handled correctly. Fix them in https://github.com/mild-blue/covid-vaxx/issues/139

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = listOf("a"),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = listOf(AnswerDtoInForTest(UUID.randomUUID(), "a")),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = listOf(AnswerDtoInForTest("a", "a")),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeAnswers = "a",
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
            ),
            maybeInsuranceCompany = "a",
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                confirmation = "a"
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                confirmation = ConfirmationDtoInForTest("a")
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        runPatientRegistrationWithBuilder(
            defaultPatientRegistrationBuilder(
                confirmation = WrongObjectDtoInForTest("a")
            ),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        ) { }

        return callsCollection.toList()

    }
}
