package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.PatientRegistrationDto
import blue.mild.covid.vaxx.service.PatientService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.getOrFail
import org.kodein.di.instance
import org.kodein.di.ktor.di
import pw.forst.tools.katlib.toUuid

fun Routing.patientRoutes() {
    val service by di().instance<PatientService>()

    route(Routes.patient) {
        get {
            call.respond(service.getAllPatients())
        }

        get("/{id}") {
            val patientId = call.parameters.getOrFail("id").toUuid()
            call.respond(service.getPatientById(patientId))
        }

        post {
            val patient = call.receive<PatientRegistrationDto>()
            service.savePatient(patient)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
