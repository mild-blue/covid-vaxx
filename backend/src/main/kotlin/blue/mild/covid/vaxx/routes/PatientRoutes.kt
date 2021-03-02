package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.NewPatientDto
import blue.mild.covid.vaxx.service.PatientService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import org.kodein.di.instance
import org.kodein.di.ktor.di

fun Routing.patientRoutes() {
    val service by di().instance<PatientService>()

    get(Routes.patient) {
        call.respond(service.getAllPatients())
    }

    post(Routes.patient) {
        val patient = call.receive<NewPatientDto>()
        service.savePatient(patient)
        call.respond(HttpStatusCode.NoContent)
    }
}
