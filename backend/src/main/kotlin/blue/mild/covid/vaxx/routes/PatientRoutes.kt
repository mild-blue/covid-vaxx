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
import blue.mild.covid.vaxx.utils.createLogger
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.delete
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.principal
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
@Suppress("LongMethod") // this is routing, that's fine
fun NormalOpenAPIRoute.patientRoutes() {
    val logger = createLogger("PatientRoutes")

    val captchaService by di().instance<RequestVerificationService>()

    val patientService by di().instance<PatientService>()
    val emailService by di().instance<MailService>()
    val medicalRegistrationService by di().instance<MedicalRegistrationService>()

    route(Routes.patient) {
        post<CaptchaVerificationDtoIn, Unit, PatientRegistrationDtoIn>(
            info("Save patient registration to the database.")
        ) { (recaptchaToken), patientRegistration ->
            val host = request.determineRealIp()
            logger.debug { "Patient registration request. Executing captcha verification." }
            captchaService.verify(recaptchaToken, host)
            logger.debug { "Captcha token verified. Saving registration." }

            val patient = patientService.savePatient(PatientRegistrationDto(patientRegistration, host))
            logger.info { "Registration created for patient ${patient.patientId}." }
            logger.debug { "Adding email to the queue." }
            emailService.sendEmail(
                PatientEmailRequestDto(
                    firstName = patientRegistration.firstName,
                    lastName = patientRegistration.lastName,
                    email = patientRegistration.email,
                    patientId = patient.patientId
                )
            )
            logger.debug { "Email request registered. Registration successful." }
            respondWithStatus(HttpStatusCode.OK)
        }
    }
    // admin routes for registered users only
    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route(Routes.adminSectionPatient) {
            route("single") {
                get<PatientByPersonalNumberQueryDtoIn, PatientDtoOut, UserPrincipal>(
                    info("Get patient by personal number.")
                ) { (personalNumber) ->
                    val principal = principal()
                    if (logger.isDebugEnabled) {
                        logger.debug { "User ${principal.userId} search by personalNumber=${personalNumber}." }
                    } else {
                        logger.info { "User ${principal.userId} search by personal number." }
                    }

                    val patient = patientService.getPatientsByPersonalNumber(personalNumber)
                    logger.debug { "Patient found under id ${patient.id}." }
                    respond(patient)
                }

                get<PatientIdDtoIn, PatientDtoOut, UserPrincipal>(
                    info("Get user by ID.")
                ) { (patientId) ->
                    val principal = principal()
                    logger.info { "User ${principal.userId} search by patientId=${patientId}." }
                    val patient = patientService.getPatientById(patientId)
                    respond(patient)
                }

                delete<PatientIdDtoIn, Unit, UserPrincipal>(
                    info("Delete user by ID.")
                ) { (patientId) ->
                    val principal = principal()
                    logger.info { "User ${principal.userId} deleted patient ${patientId}." }
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
                    val principal = principal()
                    logger.info { "User ${principal.userId} requests update of patient ${patientId}: $updateDto." }

                    patientService.updatePatientWithChangeSet(patientId, updateDto)
                    logger.info { "Patient $patientId updated." }

                    if (updateDto.vaccinatedOn != null) { // register patient if the change set indicates that the patient was vaccinated
                        logger.info { "Patient $patientId was vaccinated, registering in ISIN." }
                        medicalRegistrationService.registerPatientsVaccination(patientId)
                        logger.info { "Patient $patientId registered in ISIN." }
                    }
                    respondWithStatus(HttpStatusCode.OK)
                }
            }

            route("filter") {
                get<MultiplePatientsQueryDtoOut, List<PatientDtoOut>, UserPrincipal>(
                    info("Get patient the parameters. Filters by and clause. Empty parameters return all patients.")
                ) { patientQuery ->
                    val principal = principal()
                    logger.info { "User ${principal.userId} search query: $patientQuery." }

                    val patients = patientService.getPatientsByConjunctionOf(
                        email = patientQuery.email,
                        phoneNumber = patientQuery.phoneNumber,
                        vaccinated = patientQuery.vaccinated
                    )

                    logger.info { "Found ${patients.size} records." }
                    logger.debug { "Returning patients: ${patients.joinToString(", ") { it.id.toString() }}." }

                    respond(patients)
                }
            }
        }
    }
}
