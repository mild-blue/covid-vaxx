package blue.mild.covid.vaxx.jobs

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.internal.PatientEmailRequestDto
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.MailService
import kotlinx.coroutines.runBlocking
import mu.KLogging
import mu.Marker
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.MarkerFactory

/**
 * Job that resends emails to registered patients that did not receive the emails yet.
 */
class EmailRetryJob(private val emailService: MailService, private val locationService: LocationService) : Job {

    private companion object : KLogging() {
        val marker: Marker = MarkerFactory.getMarker("job:${EmailRetryJob::class.simpleName}")
    }

    override fun execute(): Unit = runBlocking {
        logger.debug(marker) { "Fetching locations." }
        val locations = getLocationsMap()
        logger.debug(marker) { "Generating email requests." }
        val emailRequests = getEmailRequests(locations)
        logger.info(marker) { "Preparing: ${emailRequests.size} emails." }
        emailRequests.forEach { emailService.sendEmail(it) }
        logger.info(marker) { "Emails requests registered." }
    }

    private fun getEmailRequests(locations: Map<EntityId, LocationDtoOut>) = transaction {
        Patients
            .leftJoin(VaccinationSlots)
            .select { Patients.registrationEmailSent.isNull() and VaccinationSlots.id.isNotNull() }
            .map {
                PatientEmailRequestDto(
                    firstName = it[Patients.firstName],
                    lastName = it[Patients.lastName],
                    email = it[Patients.email],
                    patientId = it[Patients.id],
                    slot = VaccinationSlotDtoOut(
                        id = it[VaccinationSlots.id],
                        locationId = it[VaccinationSlots.locationId],
                        patientId = it[Patients.id],
                        queue = it[VaccinationSlots.queue],
                        from = it[VaccinationSlots.from],
                        to = it[VaccinationSlots.to]
                    ).toRoundedSlot(),
                    location = locations.getValue(it[VaccinationSlots.locationId]),
                    // if the email sending already failed, it can be because the MailJet was not able
                    // to send it -> maybe the address is invalid, so we don't want to waste a lot of time here
                    attemptLeft = 1
                )
            }
    }

    private suspend fun getLocationsMap() = locationService.getAllLocations().associateBy { it.id }
}
