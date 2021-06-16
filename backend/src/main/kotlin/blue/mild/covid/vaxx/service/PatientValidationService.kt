package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.PatientValidationResultDto

fun interface PatientValidationService {
    /**
     * Validates patient against medical system service.
     */
    suspend fun validatePatient(
        firstName: String,
        lastName: String,
        personalNumber: String?,
        insuranceNumber: String?
    ): PatientValidationResultDto
}
