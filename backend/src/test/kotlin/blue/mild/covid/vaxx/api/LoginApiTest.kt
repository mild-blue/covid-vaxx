package blue.mild.covid.vaxx.api

import blue.mild.covid.vaxx.platform.RequestMetric
import blue.mild.covid.vaxx.utils.LoginApiTestHelper
import blue.mild.covid.vaxx.utils.LoginUserDtoInForTest
import blue.mild.covid.vaxx.utils.WrongObjectDtoInForTest
import mu.KLogging
import org.apache.http.HttpStatus

class LoginApiTest(
    targetHost: String
) : LoginApiTestHelper(targetHost, 10) {

    private companion object : KLogging()


    override suspend fun execute(): List<RequestMetric> {

        runPatientLogin(
            LoginUserDtoInForTest(1, 1),
            httpStatus = HttpStatus.SC_UNAUTHORIZED
        )

        runPatientLogin(
            LoginUserDtoInForTest("mildblue", "bluemild"),
            httpStatus = HttpStatus.SC_OK
        )

        runPatientLogin(
            WrongObjectDtoInForTest(),
            httpStatus = HttpStatus.SC_BAD_REQUEST
        )

        return callsCollection.toList()

    }
}
