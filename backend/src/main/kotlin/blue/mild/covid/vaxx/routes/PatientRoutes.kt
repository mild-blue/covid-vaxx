package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.internal.PatientEmailRequestDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.dto.request.query.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.query.MultiplePatientsQueryDtoIn
import blue.mild.covid.vaxx.dto.request.query.PatientByPersonalNumberQueryDtoIn
import blue.mild.covid.vaxx.dto.request.query.PatientIdDtoIn
import blue.mild.covid.vaxx.dto.response.OK
import blue.mild.covid.vaxx.dto.response.Ok
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegistrationResponseDtoOut
import blue.mild.covid.vaxx.error.IsinValidationException
import blue.mild.covid.vaxx.extensions.asContextAware
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.security.ddos.RequestVerificationService
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.PatientValidationService
import blue.mild.covid.vaxx.service.VaccinationSlotService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.delete
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.auth.put
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Routes related to patient entity.
 */
@Suppress("LongMethod") // this is routing, that's fine plus we need not to save
// patient in case any issue during processing happens
fun NormalOpenAPIRoute.patientRoutes() {
    val logger = createLogger("PatientRoutes")

    val captchaService by closestDI().instance<RequestVerificationService>()

    val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()
    val locationService by closestDI().instance<LocationService>()
    val patientService by closestDI().instance<PatientService>()
    val emailService by closestDI().instance<MailService>()
    val patientValidation by closestDI().instance<PatientValidationService>()

    route(Routes.patient) {
        post<CaptchaVerificationDtoIn, PatientRegistrationResponseDtoOut, PatientRegistrationDtoIn>(
            info("Save patient registration to the database.")
        ) { (recaptchaToken), patientRegistration ->
            logger.info {
                "Patient registration request - personal number ${patientRegistration.personalNumber}. Executing captcha verification."
            }
            captchaService.verify(recaptchaToken, request.determineRealIp())
            logger.info { "Captcha token verified. Validating ISIN." }

            val patientIsinId: String? = if (patientRegistration.personalNumber != null) {
                // TODO  maybe validate received input before actually using ISIN
                val patientValidationResult = patientValidation.validatePatient(patientRegistration)

                when (patientValidationResult.status) {
                    PatientValidationResult.PATIENT_FOUND ->
                        patientValidationResult.patientId
                    PatientValidationResult.PATIENT_NOT_FOUND ->
                        throw IsinValidationException(patientValidationResult)
                    PatientValidationResult.WAS_NOT_VERIFIED -> {
                        logger.warn { "Patient was not validated in isin due to some problem. Skipping isin validation." }
                        null
                    }
                }
            } else {
                logger.debug { "Personal number not set. Skipping isin validation" }
                null
            }

            // TODO maybe run this in the transaction and then do rollback
            val patientId = patientService.savePatient(asContextAware(patientRegistration), patientIsinId)
            logger.info { "Patient saved to the database with id: ${patientId}. Booking slot." }

            val (slot, location) = runCatching {
                val slot = vaccinationSlotService.bookSlotForPatient(patientId)
                    .toRoundedSlot() // display rounded data

                val location = locationService.getLocationById(slot.locationId)

                logger.info { "Slot booked: ${slot.id} for patient $patientId - registration completed." }
                logger.debug { "Adding email to the queue." }
                emailService.sendEmail(
                    PatientEmailRequestDto(
                        firstName = patientRegistration.firstName,
                        lastName = patientRegistration.lastName,
                        email = patientRegistration.email,
                        patientId = patientId,
                        slot = slot,
                        location = location
                    )
                )
                slot to location
            }.onFailure {
                logger.error {
                    "It was not possible to complete registration process for patient $patientId -" +
                            "personal number ${patientRegistration.personalNumber}. See additional logs."
                }
                patientService.deletePatientById(patientId)
            }.getOrThrow()
            logger.info { "Registration successful for patient $patientId." }

            respond(
                PatientRegistrationResponseDtoOut(
                    patientId = patientId,
                    slot = slot,
                    location = location
                )
            )
        }
    }

    // admin routes for registered users only
    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route(Routes.adminSectionPatient) {
            get<PatientByPersonalNumberQueryDtoIn, PatientDtoOut, UserPrincipal>(
                info("Get patient by personal number.")
            ) { (personalNumber) ->
                val principal = principal()
                if (logger.isDebugEnabled) {
                    logger.debug { "User ${principal.userId} search by personalNumber=${personalNumber}." }
                } else {
                    logger.info { "User ${principal.userId} search by personal number." }
                }

                val patient = patientService.getPatientByPersonalNumber(personalNumber)
                logger.debug { "Patient found under id ${patient.id}." }
                respond(patient)
            }

            get<PatientIdDtoIn, PatientDtoOut, UserPrincipal>(
                info("Get patient by ID.")
            ) { (patientId) ->
                respond(patientService.getPatientById(patientId))
            }

            delete<PatientIdDtoIn, Ok, UserPrincipal>(
                info("Delete user by ID.")
            ) { (patientId) ->
                val principal = principal()
                logger.info { "User ${principal.userId} deleted patient ${patientId}." }
                patientService.deletePatientById(patientId)
                respond(OK)
            }

            put<PatientIdDtoIn, PatientDtoOut, PatientUpdateDtoIn, UserPrincipal>(
                info(
                    "Updates patient with given change set " +
                            "- note you should send just the values that changed and not whole entity."
                )
            ) { (patientId), updateDto ->
                val principal = principal()
                logger.info { "User ${principal.userId} requests update of patient ${patientId}: $updateDto." }

                patientService.updatePatientWithChangeSet(patientId, updateDto)
                logger.info { "Patient $patientId updated." }

                respond(patientService.getPatientById(patientId))
            }

            route("filter") {
                get<MultiplePatientsQueryDtoIn, List<PatientDtoOut>, UserPrincipal>(
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
