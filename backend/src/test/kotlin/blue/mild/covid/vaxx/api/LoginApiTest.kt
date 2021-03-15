package blue.mild.covid.vaxx.api

import blue.mild.covid.vaxx.platform.RequestMetric
import blue.mild.covid.vaxx.utils.LoginApiTestHelper
import blue.mild.covid.vaxx.utils.LoginUserDtoInForTest
import blue.mild.covid.vaxx.utils.WrongObjectDtoInForTest
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
