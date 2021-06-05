package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.internal.PatientValidationResultDto
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import mu.KLogging

interface PatientValidationService {
    /**
     * Validates patient against medical system service.
     */
    suspend fun validatePatient(registrationDto: PatientRegistrationDtoIn): PatientValidationResultDto
}

class DummyPatientValidationService : PatientValidationService {
    private companion object : KLogging()

    override suspend fun validatePatient(registrationDto: PatientRegistrationDtoIn): PatientValidationResultDto {
        logger.warn { "NOT VERIFYING patient ${registrationDto.personalNumber}. This should not be in the production." }
        return IsinValidationResultDto(PatientValidationResult.WAS_NOT_VERIFIED)
    }
}
