package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.service.IsinInterfaceService
import mu.KLogging

class DummyIsinService : IsinInterfaceService {

    private companion object : KLogging()

    override suspend fun getPatientByParameters(
        jmeno: String, prijmeni: String, rodneCislo: String
    ): IsinGetPatientByParametersResultDto {
        logger.warn { "NOT GETTING patient ${jmeno}/${prijmeni}/${rodneCislo} from ISIN. This should not be in the production." }
        return IsinGetPatientByParametersResultDto("PacientNebylNalezen", null, null)
    }

    override suspend fun tryExportPatientContactInfo(patient: PatientDtoOut, notes: String?): Boolean {
        logger.warn { "NOT EXPORTING patient ${patient.personalNumber} to ISIN. This should not be in the production." }
        return false
    }
}
