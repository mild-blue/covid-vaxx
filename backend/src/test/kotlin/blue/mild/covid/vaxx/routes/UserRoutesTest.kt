package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.request.CredentialsDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.response.UserLoginResponseDtoOut
import blue.mild.covid.vaxx.utils.DatabaseData
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class UserRoutesTest : ServerTestBase() {
    @Test
    fun `login should behave like expected`() = withTestApplication {
        // try to login with non-existing user
        val nonExistingUser = LoginDtoIn(
            credentials = CredentialsDtoIn("non-existing@email.com", "wrong-password"),
            nurseId = null,
            vaccineSerialNumber = ""
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(nonExistingUser) }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // existing user but wrong password
        val wrongPasswordLogin = LoginDtoIn(
            credentials = CredentialsDtoIn(DatabaseData.admin.email, UUID.randomUUID().toString()),
            nurseId = null,
            vaccineSerialNumber = ""
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(wrongPasswordLogin) }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // try to login with existing user but with non existing nurse
        val nonExistingNurse = UUID.randomUUID()
        // assert that we indeed created nurse that does not exist
        assertTrue { DatabaseData.nurses.none { it.id == nonExistingNurse } }
        val existingUserNonExistingNurse = LoginDtoIn(
            credentials = CredentialsDtoIn(DatabaseData.admin.email, DatabaseData.admin.password),
            nurseId = nonExistingNurse,
            vaccineSerialNumber = ""
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(existingUserNonExistingNurse) }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // existing user and existing nurse
        val validLogin = LoginDtoIn(
            credentials = CredentialsDtoIn(DatabaseData.admin.email, DatabaseData.admin.password),
            nurseId = DatabaseData.nurses.random().id,
            vaccineSerialNumber = "#123"
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(validLogin) }.run {
            expectStatus(HttpStatusCode.OK)
            val response = receive<UserLoginResponseDtoOut>()
            assertEquals(DatabaseData.admin.role, response.role)
        }
    }
}
