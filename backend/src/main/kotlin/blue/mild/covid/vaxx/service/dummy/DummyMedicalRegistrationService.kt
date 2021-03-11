package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.service.MedicalRegistrationService
import mu.KLogging
import java.util.UUID

class DummyMedicalRegistrationService : MedicalRegistrationService {

    private companion object : KLogging()

    override suspend fun registerPatientsVaccination(patientId: UUID) {
        logger.warn { "NOT REGISTERING vaccination for patient with id $patientId" }
    }
}
