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
        logger.warn { "NOT GETTING patient ${firstName}/${lastName}/${personalNumber} from ISIN. This should not be in the production." }
        return IsinGetPatientByParametersResultDto("PouzitiDummyISIN", null, null)
    }

    override suspend fun tryExportPatientContactInfo(
        patient: PatientDtoOut,
        notes: String?
    ): Boolean {
        logger.warn { "NOT EXPORTING patient ${patient.personalNumber} to ISIN. This should not be in the production." }
        return false
    }

    override suspend fun tryCreateVaccinationAndDose(
        vaccination: StoreVaccinationRequestDto,
        patient: PatientDtoOut
    ): Boolean {
        logger.warn { "NOT EXPORTING vaccination ${vaccination.vaccinationId} to ISIN. This should not be in the production." }
        return false
    }
}
