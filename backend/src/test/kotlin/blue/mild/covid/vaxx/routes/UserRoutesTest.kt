package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.CredentialsDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.UserLoginResponseDtoOut
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.utils.DatabaseData
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserRoutesTest : ServerTestBase() {

    @Test
    fun `test create new user`() = withTestApplication {
        val user = UserRegistrationDtoIn(
            email = "${UUID.randomUUID()}@mild.blue",
            password = UUID.randomUUID().toString(),
            firstName = "Mild",
            lastName = "Blue",
            role = UserRole.ADMIN
        )
        // try to register user without logging in first
        handleRequest(HttpMethod.Post, Routes.userRegistration) {
            jsonBody(user)
        }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // try to register user with wrong role of Doctor as only Admin can access the endpoint
        handleRequest(HttpMethod.Post, Routes.userRegistration) {
            authorize(UserPrincipal(
                UUID.randomUUID(), userRole = UserRole.DOCTOR, vaccineSerialNumber = "", LocalDate.now()
            ))
            jsonBody(user)
        }.run {
            expectStatus(HttpStatusCode.Forbidden)
        }

        // successful registration
        handleRequest(HttpMethod.Post, Routes.userRegistration) {
            authorize()
            jsonBody(user)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val out = receive<UserRegisteredDtoOut>()
            assertNotNull(out.id)
        }

        // try to login
        // existing user and existing nurse
        val validLogin = LoginDtoIn(
            credentials = CredentialsDtoIn(user.email, user.password),
            nurseId = DatabaseData.nurses.random().id,
            vaccineSerialNumber = "#123",
            vaccineExpiration = LocalDate.now()
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) {
            jsonBody(validLogin)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val response = receive<UserLoginResponseDtoOut>()
            assertEquals(user.role, response.role)
        }
    }

    @Test
    @Disabled
    fun `user login verification should return OK when user is logged in`() {
        // Implement test that will call Routes.userLoginVerification and checks
        // that if the request is authorized, then the status code is OK
        // when no authorization headers are present the response should be Unauthorized

        // hint: see the test above or below
        TODO("#258 implement me")
    }

    @Test
    fun `login should behave like expected`() = withTestApplication {
        // try to login with non-existing user
        val nonExistingUser = LoginDtoIn(
            credentials = CredentialsDtoIn("non-existing@email.com", "wrong-password"),
            nurseId = null,
            vaccineSerialNumber = "",
            vaccineExpiration = LocalDate.now()
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(nonExistingUser) }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // existing user but wrong password
        val wrongPasswordLogin = LoginDtoIn(
            credentials = CredentialsDtoIn(DatabaseData.admin.email, UUID.randomUUID().toString()),
            nurseId = null,
            vaccineSerialNumber = "",
            vaccineExpiration = LocalDate.now()
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
            vaccineSerialNumber = "",
            vaccineExpiration = LocalDate.now()
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(existingUserNonExistingNurse) }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // existing user and existing nurse
        val validLogin = LoginDtoIn(
            credentials = CredentialsDtoIn(DatabaseData.admin.email, DatabaseData.admin.password),
            nurseId = DatabaseData.nurses.random().id,
            vaccineSerialNumber = "#123",
            vaccineExpiration = LocalDate.now()
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(validLogin) }.run {
            expectStatus(HttpStatusCode.OK)
            val response = receive<UserLoginResponseDtoOut>()
            assertEquals(DatabaseData.admin.role, response.role)
        }
    }
}
