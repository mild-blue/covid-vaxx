package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import mu.KLogging
import java.util.Locale


class IsinValidationService(
    private val configuration: IsinConfigurationDto,
    private val isinClient: HttpClient
) : PatientValidationService {

    private val userIdentification =
        "?pcz=${configuration.pracovnik.pcz}&pracovnikNrzpCislo=${configuration.pracovnik.nrzpCislo}"

    private companion object : KLogging() {
        const val URL_NAJDI_PACIENTA = "pacienti/VyhledatDleJmenoPrijmeniRc"
    }

    private enum class VyhledaniPacientaResult {
        PacientNalezen,
        NalezenoVicePacientu,
        PacientNebylNalezen,
        CizinecZaloz,
        ChybaVstupnichDat,
        Chyba
    }

    override suspend fun validatePatient(registrationDto: PatientRegistrationDtoIn): IsinValidationResultDto {
        val firstName = registrationDto.firstName.trim().uppercase(Locale.getDefault())
        val lastName = registrationDto.lastName.trim().uppercase(Locale.getDefault())
        val personalNumber = registrationDto.personalNumber.normalizePersonalNumber()

        val response = runCatching {
            getPatientResponse(
                jmeno = firstName,
                prijmeni = lastName,
                rodneCislo = personalNumber
            )
        }.getOrElse {
            logger.error(it) {
                "Getting data from ISIN server failed for patient ${firstName}/${lastName}/${personalNumber}"
            }
            return IsinValidationResultDto( status = PatientValidationResult.WAS_NOT_VERIFIED )
        }

        val json = response.receive<JsonNode>()
        val result = json.get("vysledek")?.textValue()
        val resultMessage = json.get("vysledekZprava")?.textValue()
        val patientId = json.get("pacient")?.get("id")?.textValue()

        logger.info {
            "Data from ISIN for patient ${firstName}/${lastName}/${personalNumber}: " +
            "result=${result}, resultMessage=${resultMessage}, patientId=${patientId}."
        }

        return when (result) {
            VyhledaniPacientaResult.PacientNalezen.name,
            VyhledaniPacientaResult.NalezenoVicePacientu.name ->
                IsinValidationResultDto(
                    status = PatientValidationResult.PATIENT_FOUND,
                    patientId = patientId
                )
            VyhledaniPacientaResult.PacientNebylNalezen.name,
            VyhledaniPacientaResult.ChybaVstupnichDat.name ->
                IsinValidationResultDto(
                    status = PatientValidationResult.PATIENT_NOT_FOUND
                )
            else ->
                IsinValidationResultDto(
                    status = PatientValidationResult.WAS_NOT_VERIFIED
                )
        }
    }

    private suspend fun getPatientResponse(jmeno: String, prijmeni: String, rodneCislo: String): HttpResponse {
        val url = createIsinURL(URL_NAJDI_PACIENTA, parameters = listOf(jmeno, prijmeni, rodneCislo))
        return isinClient.get(url)
    }

    private fun createIsinURL(
        requestUrl: String,
        baseUrl: String = configuration.rootUrl,
        parameters: List<Any> = listOf(),
        includeIdentification: Boolean = true
    ): String {
        val parametersUrl = parameters.joinToString(separator = "/") { it.toString() }
        return "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
    }
}
