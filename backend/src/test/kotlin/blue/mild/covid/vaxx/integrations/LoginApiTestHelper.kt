package blue.mild.covid.vaxx.integrations

import blue.mild.covid.vaxx.routes.Routes
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import mu.KLogging


abstract class LoginApiTestHelper(
   targetHost: String,
   requestTimeoutsSeconds: Int
) : ApiCallTestHelper(targetHost, requestTimeoutsSeconds) {
    private companion object : KLogging()

    protected suspend fun runPatientLogin(
        loginUserForTest: Any,
        httpStatus: HttpStatusCode = HttpStatusCode.OK,
    ) {
            val request = loginUser(loginUserForTest)
            require(request.status == httpStatus) {
                "Login did not run as expected " +
                        "expected was status code $httpStatus but got ${request.status.value} ${request.status.description}" +
                        "${request.body<JsonNode>()}"
            }
        counter.incrementAndGet()
    }

    private suspend fun loginUser(loginUserForTest: Any) =
        meteredClient.post("${targetHost}${Routes.registeredUserLogin}") { // should be disabled
            contentType(ContentType.Application.Json)
            accept(ContentType.Companion.Any)
            setBody(loginUserForTest)
        }
}
