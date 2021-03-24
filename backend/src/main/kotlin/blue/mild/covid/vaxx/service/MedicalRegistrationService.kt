package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto

interface MedicalRegistrationService {
    /**
     * Register that the patient with given [patientVaccination] was vaccinated in the remote service.
     */
    suspend fun registerPatientsVaccination(patientVaccination: StoreVaccinationRequestDto)
}
