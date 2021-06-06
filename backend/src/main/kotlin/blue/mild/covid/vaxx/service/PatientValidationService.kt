package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.PatientValidationResultDto
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn

fun interface PatientValidationService {
    /**
     * Validates patient against medical system service.
     */
    suspend fun validatePatient(registrationDto: PatientRegistrationDtoIn): PatientValidationResultDto
}
