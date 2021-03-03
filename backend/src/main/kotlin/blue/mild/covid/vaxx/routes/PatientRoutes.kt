package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.PatientDeletedDtoOut
import blue.mild.covid.vaxx.dto.PatientDtoOut
import blue.mild.covid.vaxx.dto.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.service.PatientService
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.delete
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.LazyDI
import org.kodein.di.instance
import pw.forst.tools.katlib.asList
import java.util.UUID

@Path("{id}")
data class PatientIdDtoIn(@PathParam("Patient ID") val id: UUID)

data class PatientQueryDtoIn(
    @QueryParam("Patient ID") val id: UUID?,
    @QueryParam("Patient personal number") val personalNumber: String?,
    @QueryParam("Patient email") val email: String?
)

data class PatientCreatedDtoOut(val patientId: String)

fun NormalOpenAPIRoute.patientRoutes(di: LazyDI) {
    val service by di.instance<PatientService>()

    route(Routes.patient) {
        get<PatientIdDtoIn, PatientDtoOut> { (id) ->
            respond(service.getPatientById(id))
        }

        delete<PatientIdDtoIn, PatientDeletedDtoOut> { (id) ->
            respond(service.deletePatientById(id))
        }

        get<PatientQueryDtoIn, List<PatientDtoOut>> { patientQuery ->
            val response = when {
                patientQuery.id != null -> service.getPatientById(patientQuery.id).asList()
                patientQuery.personalNumber != null -> service.getPatientsByPersonalNumber(patientQuery.personalNumber)
                patientQuery.email != null -> service.getPatientsByEmail(patientQuery.email)
                else -> service.getAllPatients()
            }
            respond(response)
        }

        post<Unit, PatientCreatedDtoOut, PatientRegistrationDtoIn> { _, patientRegistration ->
            val patientId = service.savePatient(patientRegistration)
            respond(PatientCreatedDtoOut(patientId))
        }
    }
}
