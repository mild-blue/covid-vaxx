package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.response.PatientDtoOut

interface IsinInterfaceService {
    suspend fun getPatientByParameters(
        jmeno: String, prijmeni: String, rodneCislo: String
    ): IsinGetPatientByParametersResultDto

    suspend fun tryExportPatientContactInfo(patient: PatientDtoOut, notes: String?): Boolean
}
