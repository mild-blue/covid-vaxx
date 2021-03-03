package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.PatientDtoOut
import blue.mild.covid.vaxx.dto.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.service.PatientService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
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
            val params = call.request.queryParameters

            val response: Any = when {
                params.contains(PatientDtoOut::id.name) ->
                    service.getPatientById(params.getOrFail(PatientDtoOut::id.name).toUuid())
                params.contains(PatientDtoOut::personalNumber.name) ->
                    service.getPatientsByPersonalNumber(params.getOrFail(PatientDtoOut::personalNumber.name))
                params.contains(PatientDtoOut::email.name) ->
                    service.getPatientsByEmail(params.getOrFail(PatientDtoOut::email.name))
                else -> service.getAllPatients()
            }
            call.respond(response)
        }

        get("/{id}") {
            val patientId = call.parameters.getOrFail("id").toUuid()
            call.respond(service.getPatientById(patientId))
        }

        delete("/{id}") {
            val patientId = call.parameters.getOrFail("id").toUuid()
            call.respond(service.deletePatientById(patientId))
        }

        post {
            val patient = call.receive<PatientRegistrationDtoIn>()
            service.savePatient(patient)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
