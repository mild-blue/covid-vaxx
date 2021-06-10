package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import mu.KLogging


class IsinValidationService(
    private val isinService: IsinServiceInterface
) : PatientValidationService {

    private enum class VyhledaniPacientaResult {
        PacientNalezen,
        NalezenoVicePacientu,
        PacientNebylNalezen,
        CizinecZaloz,
        ChybaVstupnichDat,
        Chyba
    }

    private companion object : KLogging()

    override suspend fun validatePatient(registrationDto: PatientRegistrationDtoIn): IsinValidationResultDto {
        val firstName = registrationDto.firstName
        val lastName = registrationDto.lastName
        val personalNumber = registrationDto.personalNumber
            ?: throw IllegalArgumentException("Personal number cannot be null for ISIN validation")

        val result = runCatching {
            isinService.getPatientByParameters(
                firstName = firstName,
                lastName = lastName,
                personalNumber = personalNumber
            )
        }.onSuccess {
            logger.info { "Data retrieval from ISIN - success." }
        }.onFailure {
            logger.warn { "Data retrieval from ISIN - failure." }
            // TODO we think that this is the place which produces stack overflow
            logger.error(it) {
                "Getting data from ISIN server failed for patient ${firstName}/${lastName}/${personalNumber}"
            }
        }.getOrNull() ?: return IsinValidationResultDto(status = PatientValidationResult.WAS_NOT_VERIFIED)

        logger.info {
            "Data from ISIN for patient ${firstName}/${lastName}/${personalNumber}: " +
            "result=${result.result}, resultMessage=${result.resultMessage}, patientId=${result.patientId}."
        }

        return when (result.result) {
            VyhledaniPacientaResult.PacientNalezen.name,
            VyhledaniPacientaResult.NalezenoVicePacientu.name ->
                IsinValidationResultDto(
                    status = PatientValidationResult.PATIENT_FOUND,
                    patientId = result.patientId
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
}
