@file:Suppress("UNREACHABLE_CODE")

package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.repository.NurseRepository
import blue.mild.covid.vaxx.dto.request.CredentialsDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.NurseCreationDtoIn
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.utils.DatabaseData
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NurseRoutesTest : ServerTestBase() {
    @Test
    // @Disabled
    fun `should respond with all nurses`() = withTestApplication {
      //  TODO("implement this test for file NurseRoutes.kt")
        // verify two cases:
        // 1. when user submits correct credentials, server should respond with all nurses in the database
        val validLogin= LoginDtoIn(
            credentials = CredentialsDtoIn("${UUID.randomUUID()}@mild.blue", UUID.randomUUID().toString()),
            nurseId = DatabaseData.nurses.random().id,
            vaccineSerialNumber = "#123",
            vaccineExpiration =  LocalDate.now()
        )

        handleRequest(HttpMethod.Post, Routes.registeredUserLogin){
            jsonBody(validLogin)
        }.run{
            println("correct credentials")
            handleRequest(HttpMethod.Get, Routes.nurse) {
                authorize()
        }
        // 2. in case of invalid credentials, the server should respond with status 401
        val invalidLogin = LoginDtoIn(
            credentials = CredentialsDtoIn("non-existing@email.com", "wrong-password"),
            nurseId = null,
            vaccineSerialNumber = "",
            vaccineExpiration = LocalDate.now()
        )
        handleRequest(HttpMethod.Post, Routes.registeredUserLogin) { jsonBody(invalidLogin) }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }
        // very similar test is for example UserRoutesTest, where we try to log in and then check
        // what status code the server returned

        // check the next test case to see how to get all nurses

        // once the test is created, delete @Disabled annotation and this TODO comments
    }

    @Test
    fun `nurse should be created only by admin`() = withTestApplication {
        val nurseRepository by closestDI().instance<NurseRepository>()
        // fetch all nurses to show how one can do that
        val allNursesBeforeCreation = runBlocking { nurseRepository.getAll() }

        val nurseToCreate = NurseCreationDtoIn(
            email = "${UUID.randomUUID()}@mild.blue",
            firstName = "Michel",
            lastName = "Burnham"
        )
        // verify that the nurse to be created does not exist yet
        assertTrue { allNursesBeforeCreation.none { it.email == nurseToCreate.email } }
        // try to create nurse without authorization
        handleRequest(HttpMethod.Put, Routes.nurse) {
            jsonBody(nurseToCreate)
        }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }
        // try to create nurse as Doctor
        handleRequest(HttpMethod.Put, Routes.nurse) {
            // we can safely use ID that exists with different role
            authorize(UserPrincipal(
                DatabaseData.admin.id, UserRole.DOCTOR,
                "", LocalDate.now(),null
            ))
            jsonBody(nurseToCreate)
        }.run {
            expectStatus(HttpStatusCode.Forbidden)
        }

        // verify that non of these requests created the nurse
        val nursesWithRandomEmailCount = transaction { Nurses.select { Nurses.email eq nurseToCreate.email }.count() }
        assertEquals(0, nursesWithRandomEmailCount)

        // create nurse
        handleRequest(HttpMethod.Put, Routes.nurse) {
            authorize()
            jsonBody(nurseToCreate)
        }.run {
            expectStatus(HttpStatusCode.OK)
        }
        // verify that the nurse was created
        val nurseRow = transaction {
            Nurses.select { Nurses.email eq nurseToCreate.email }.singleOrNull()
        }

        assertNotNull(nurseRow)
        assertEquals(nurseToCreate.email, nurseRow[Nurses.email])
        assertEquals(nurseToCreate.firstName, nurseRow[Nurses.firstName])
        assertEquals(nurseToCreate.lastName, nurseRow[Nurses.lastName])
    }
}
