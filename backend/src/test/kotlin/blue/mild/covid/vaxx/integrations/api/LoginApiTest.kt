package blue.mild.covid.vaxx.integrations.api

import blue.mild.covid.vaxx.integrations.LoginApiTestHelper
import blue.mild.covid.vaxx.integrations.LoginUserDtoInForTest
import blue.mild.covid.vaxx.integrations.WrongObjectDtoInForTest
import blue.mild.covid.vaxx.integrations.platform.RequestMetric
import io.ktor.http.HttpStatusCode
import mu.KLogging

class LoginApiTest(
    targetHost: String
) : LoginApiTestHelper(targetHost, 10) {

    private companion object : KLogging()


    override suspend fun execute(): List<RequestMetric> {

        runPatientLogin(
            LoginUserDtoInForTest(1, 1),
            httpStatus = HttpStatusCode.Unauthorized
        )

        runPatientLogin(
            LoginUserDtoInForTest("mildblue", "bluemild"),
        )

        runPatientLogin(
            WrongObjectDtoInForTest(),
            httpStatus = HttpStatusCode.BadRequest
        )

        return callsCollection.toList()

    }
}
