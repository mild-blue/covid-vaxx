package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.service.IsinServiceInterface
import mu.KLogging

class DummyIsinService : IsinServiceInterface {

    private companion object : KLogging()

    override suspend fun getPatientByParameters(
        firstName: String,
        lastName: String,
        personalNumber: String
    ): IsinGetPatientByParametersResultDto {
        logger.warn { "NOT GETTING patient $firstName/$lastName/$personalNumber from ISIN. This should not be used in production." }
        return IsinGetPatientByParametersResultDto("UsingDummyISIN", null, null)
    }

    override suspend fun getForeignerByInsuranceNumber(
        insuranceNumber: String
    ): IsinGetPatientByParametersResultDto {
        logger.warn { "NOT GETTING foreigner $insuranceNumber from ISIN. This should not be used in production." }
        return IsinGetPatientByParametersResultDto("UsingDummyISIN", null, null)
    }

    override suspend fun tryPatientIsReadyForVaccination(isinId: String): Boolean? {
        logger.warn { "NOT GETTING vaccinations of patient $isinId from ISIN. This should not be used in production." }
        return null
    }

    override suspend fun tryExportPatientContactInfo(
        patient: PatientDtoOut,
        notes: String?
    ): Boolean {
        logger.warn { "NOT EXPORTING patient ${patient.personalNumber} to ISIN. This should not be used in production." }
        return false
    }

    override suspend fun tryCreateVaccination(
        vaccination: StoreVaccinationRequestDto,
        patient: PatientDtoOut
    ): Boolean {
        logger.warn { "NOT EXPORTING vaccination ${vaccination.vaccinationId} to ISIN. This should not be used in production." }
        return false
    }

    // This is used to clean the test data and should never be used in production
    override suspend fun cancelAllVaccinations(
        isinId: String
    ) {
        logger.warn { "NOT CANCELLING vaccinations for patient $isinId in ISIN. This should not be used in production." }
    }
}
