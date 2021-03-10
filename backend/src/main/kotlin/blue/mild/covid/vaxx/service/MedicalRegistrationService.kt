package blue.mild.covid.vaxx.service

import java.util.UUID

interface MedicalRegistrationService {
    /**
     * Register that the patient with given [patientId] was vaccinated in the remote service.
     */
    suspend fun registerPatientsVaccination(patientId: UUID)
}
