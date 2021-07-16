package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto
import blue.mild.covid.vaxx.dto.response.PatientDtoOut

interface IsinServiceInterface {
    suspend fun getPatientByParameters(
        firstName: String,
        lastName: String,
        personalNumber: String
    ): IsinGetPatientByParametersResultDto

    suspend fun getForeignerByInsuranceNumber(
        insuranceNumber: String
    ): IsinGetPatientByParametersResultDto

    suspend fun tryPatientIsReadyForVaccination(isinId: String): Boolean?

    suspend fun tryExportPatientContactInfo(
        patient: PatientDtoOut,
        notes: String?
    ): Boolean

    suspend fun tryCreateVaccination(
        vaccination: StoreVaccinationRequestDto,
        patient: PatientDtoOut
    ): Boolean

    // This is used to clean the test data and should never be used in production
    suspend fun cancelAllVaccinations(
        isinId: String
    )
}
