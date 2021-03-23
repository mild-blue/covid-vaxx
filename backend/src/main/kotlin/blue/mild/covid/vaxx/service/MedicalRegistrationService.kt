package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.PatientVaccinationDetailDto

interface MedicalRegistrationService {
    /**
     * Register that the patient with given [patientVaccination] was vaccinated in the remote service.
     */
    suspend fun registerPatientsVaccination(patientVaccination: PatientVaccinationDetailDto)
}
