package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.PatientEmailRequestDto
import blue.mild.covid.vaxx.dto.PatientRegistrationDto
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.MultiplePatientsQueryDtoOut
import blue.mild.covid.vaxx.dto.request.PatientByPersonalNumberQueryDtoIn
import blue.mild.covid.vaxx.dto.request.PatientIdDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.extensions.respondWithStatus
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.security.ddos.RequestVerificationService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.MedicalRegistrationService
import blue.mild.covid.vaxx.service.PatientService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.delete
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.put
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.HttpStatusCode
import org.kodein.di.instance
import java.time.Instant

/**
 * Routes related to patient entity.
 */
fun NormalOpenAPIRoute.patientRoutes() {
    val captchaService by di().instance<RequestVerificationService>()

    val patientService by di().instance<PatientService>()
    val emailService by di().instance<MailService>()
    val medicalRegistrationService by di().instance<MedicalRegistrationService>()

    route(Routes.patient) {
        post<CaptchaVerificationDtoIn, Unit, PatientRegistrationDtoIn>(
            info("Save patient registration to the database.")
        ) { (recaptchaToken), patientRegistration ->
            val host = request.determineRealIp()
            captchaService.verify(recaptchaToken, host)

            val patient = patientService.savePatient(PatientRegistrationDto(patientRegistration, host))
            emailService.sendEmail(
                PatientEmailRequestDto(
                    firstName = patientRegistration.firstName,
                    lastName = patientRegistration.lastName,
                    email = patientRegistration.email,
                    patientId = patient.patientId
                )
            )
            respondWithStatus(HttpStatusCode.OK)
        }
    }
    // admin routes for registered users only
    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route(Routes.patient) {
            route("single") {
                get<PatientByPersonalNumberQueryDtoIn, PatientDtoOut, UserPrincipal>(
                    info("Get patient by personal number.")
                ) { patientQuery ->
                    respond(patientService.getPatientsByPersonalNumber(patientQuery.personalNumber))
                }

                get<PatientIdDtoIn, PatientDtoOut, UserPrincipal>(
                    info("Get user by ID.")
                ) { (patientId) ->
                    respond(patientService.getPatientById(patientId))
                }

                delete<PatientIdDtoIn, Unit, UserPrincipal>(
                    info("Delete user by ID.")
                ) { (patientId) ->
                    patientService.deletePatientById(patientId)
                    respondWithStatus(HttpStatusCode.OK)
                }

                put<PatientIdDtoIn, Unit, PatientUpdateDtoIn, UserPrincipal>(
                    info(
                        "Updates patient with given change set " +
                                "- note you should send just the values that changed and not whole entity."
                    ),
                    exampleRequest = PatientUpdateDtoIn(email = "john@doe.com", vaccinatedOn = Instant.now())
                ) { (patientId), updateDto ->
                    patientService.updatePatientWithChangeSet(patientId, updateDto)
                    if (updateDto.vaccinatedOn != null) { // register patient if the change set indicates that the patient was vaccinated
                        medicalRegistrationService.registerPatientsVaccination(patientId)
                    }
                    respondWithStatus(HttpStatusCode.OK)
                }
            }

            route("filter") {
                get<MultiplePatientsQueryDtoOut, List<PatientDtoOut>, UserPrincipal>(
                    info("Get patient the parameters. Filters by and clause. Empty parameters return all patients.")
                ) { patientQuery ->
                    respond(
                        patientService.getPatientsByConjunctionOf(
                            email = patientQuery.email,
                            phoneNumber = patientQuery.phoneNumber,
                            vaccinated = patientQuery.vaccinated
                        )
                    )
                }
            }
        }
    }
}
