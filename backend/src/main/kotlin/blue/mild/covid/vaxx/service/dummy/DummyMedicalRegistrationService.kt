package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto
import blue.mild.covid.vaxx.service.MedicalRegistrationService
import mu.KLogging

class DummyMedicalRegistrationService : MedicalRegistrationService {

    private companion object : KLogging()

    override suspend fun registerPatientsVaccination(patientVaccination: StoreVaccinationRequestDto) {
        logger.warn { "NOT REGISTERING vaccination for patient with id ${patientVaccination.patientId}." }
    }
}
