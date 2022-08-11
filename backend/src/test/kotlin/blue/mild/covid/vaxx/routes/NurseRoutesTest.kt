package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.repository.NurseRepository
import blue.mild.covid.vaxx.dto.request.CredentialsDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.NurseCreationDtoIn
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import blue.mild.covid.vaxx.dto.response.UserLoginResponseDtoOut
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
    fun `should respond with all nurses`() = withTestApplication {
        // 1. when user submits correct credentials, server should respond with all nurses in the database
        val validLogin = CredentialsDtoIn(
            email = DatabaseData.admin.email,
            password = DatabaseData.admin.password
        )

        handleRequest(HttpMethod.Post, Routes.nurse) {
            jsonBody(validLogin)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val response = receive<List<PersonnelDtoOut>>()
            val nurseRepository by closestDI().instance<NurseRepository>()
            val allNurses = runBlocking { nurseRepository.getAll() }
            assertEquals(allNurses, response)
        }

        // 2. in case of invalid credentials, the server should respond with status 401
        val invalidLogin = CredentialsDtoIn(
            email = "non-existing@email.com",
            password = "wrong-password"
        )
        handleRequest(HttpMethod.Post, Routes.nurse) {
            jsonBody(invalidLogin)
        }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

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
            authorize(
                UserPrincipal(
                    DatabaseData.admin.id, UserRole.DOCTOR,
                    "", LocalDate.now(), null
                )
            )
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
