package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.UserRole
import blue.mild.covid.vaxx.dto.PatientEmailRequestDto
import blue.mild.covid.vaxx.dto.PatientRegistrationDto
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientIdDtoIn
import blue.mild.covid.vaxx.dto.request.PatientQueryDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDeletedDtoOut
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegisteredDtoOut
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.CaptchaVerificationService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.setup.EnvVariables
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.delete
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance
import pw.forst.tools.katlib.asList

/**
 * Routes related to patient entity.
 */
fun NormalOpenAPIRoute.patientRoutes() {
    val patientService by di().instance<PatientService>()
    val emailService by di().instance<MailService>()

    val captchaService by di().instance<CaptchaVerificationService>()
    val enableCaptchaVerification by di().instance<Boolean>(EnvVariables.ENABLE_SWAGGER)

    route(Routes.patient) {
        post<CaptchaVerificationDtoIn, PatientRegisteredDtoOut, PatientRegistrationDtoIn>(
            info("Save patient registration to the database.")
        ) { (recaptchaToken), patientRegistration ->
            val host = request.determineRealIp()
            if (enableCaptchaVerification) {
                captchaService.verify(recaptchaToken, host)
            }

            val patient = patientService.savePatient(PatientRegistrationDto(patientRegistration, host))
            emailService.sendEmail(
                PatientEmailRequestDto(
                    firstName = patientRegistration.firstName,
                    lastName = patientRegistration.lastName,
                    email = patientRegistration.email,
                    patientId = patient.patientId
                )
            )
            respond(patient)
        }
    }
    // admin routes for registered users only
    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route(Routes.patient) {
            get<PatientIdDtoIn, PatientDtoOut, UserPrincipal>(
                info("Get user by ID.")
            ) { (id) ->
                respond(patientService.getPatientById(id))
            }

            delete<PatientIdDtoIn, PatientDeletedDtoOut, UserPrincipal>(
                info("Delete user by ID.")
            ) { (id) ->
                respond(patientService.deletePatientById(id))
            }

            get<PatientQueryDtoIn, List<PatientDtoOut>, UserPrincipal>(
                info("Search endpoint for user, only single parameter is taken in account.")
            ) { patientQuery ->
                val response = when {
                    patientQuery.id != null -> patientService.getPatientById(patientQuery.id).asList()
                    patientQuery.personalNumber != null -> patientService.getPatientsByPersonalNumber(patientQuery.personalNumber)
                    patientQuery.email != null -> patientService.getPatientsByEmail(patientQuery.email)
                    else -> patientService.getAllPatients()
                }
                respond(response)
            }
        }
    }
}
