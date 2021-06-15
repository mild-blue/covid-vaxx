package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.internal.PatientValidationResultDto
import blue.mild.covid.vaxx.service.PatientValidationService
import mu.KLogging

class DummyPatientValidationService : PatientValidationService {
    private companion object : KLogging()

    override suspend fun validatePatient(
        firstName: String,
        lastName: String,
        personalNumber: String
    ): PatientValidationResultDto {
        logger.warn { "NOT VERIFYING patient ${personalNumber}. This should not be in the production." }
        return IsinValidationResultDto(PatientValidationResult.WAS_NOT_VERIFIED)
    }
}

