package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.DataCorrectnessDtoIn
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDetailDtoOut
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.generators.generatePatientInDatabase
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.test.assertEquals

class DataCorrectnessRoutesTest : ServerTestBase() {

    private lateinit var patient1: PatientDtoOut
    private lateinit var patient2: PatientDtoOut

    override fun populateDatabase(di: DI): Unit = runBlocking {
        val patientRepository by di.instance<PatientRepository>()

        patient1 = patientRepository.generatePatientInDatabase()
        patient2 = patientRepository.generatePatientInDatabase()
    }

    @Test
    fun `test create data correctness`() = withTestApplication {
        // verify that there's no correctness for patient1
        handleRequest(HttpMethod.Get, "${Routes.dataCorrectness}?id=${patient1.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }

        val input = DataCorrectnessDtoIn(
            patientId = patient1.id,
            dataAreCorrect = true,
            notes = "ok"
        )
        // create data correctness
        val correctnessId = handleRequest(HttpMethod.Post, Routes.dataCorrectness) {
            authorize()
            jsonBody(input)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<DataCorrectnessConfirmationDetailDtoOut>()
            assertEquals(defaultPrincipal.userId, output.doctor.id)
            assertEquals(defaultPrincipal.nurseId, output.nurse?.id)
            assertEquals(input.dataAreCorrect, output.dataAreCorrect)
            assertEquals(input.notes, output.notes)
            assertEquals(input.patientId, output.patientId)
            output.id
        }

        // verify that getting correctness by correctness id works
        handleRequest(HttpMethod.Get, "${Routes.dataCorrectness}/$correctnessId") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<DataCorrectnessConfirmationDetailDtoOut>()
            assertEquals(correctnessId, output.id)
            assertEquals(patient1.id, output.patientId)
        }

        // verify that there's no correctness for patient2 as we were registering patient1
        handleRequest(HttpMethod.Get, "${Routes.dataCorrectness}?id=${patient2.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }

        // verify that the patient1 really has the data correctness confirmation
        handleRequest(HttpMethod.Get, "${Routes.dataCorrectness}?id=${patient1.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<DataCorrectnessConfirmationDetailDtoOut>()
        }
    }
}
