package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
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

    override suspend fun validatePatient(
        firstName: String,
        lastName: String,
        personalNumber: String?,
        insuranceNumber: String?
    ): IsinValidationResultDto {
        return runCatching {
            if (personalNumber != null) {
                val result = isinService.getPatientByParameters(
                    firstName = firstName,
                    lastName = lastName,
                    personalNumber = personalNumber
                )
                logger.info {
                    "Data from ISIN for patient ${firstName}/${lastName}/${personalNumber}: " +
                    "result=${result.result}, resultMessage=${result.resultMessage}, patientId=${result.patientId}."
                }
                convertResult(result)
            } else if (insuranceNumber != null){
                val byInsuranceNumberResult = isinService.getForeignerByInsuranceNumber(
                    insuranceNumber = insuranceNumber
                )
                logger.info {
                    "Data from ISIN for foreigner ${insuranceNumber}: " +
                    "result=${byInsuranceNumberResult.result}, resultMessage=${byInsuranceNumberResult.resultMessage}, patientId=${byInsuranceNumberResult.patientId}."
                }
                val validationResult = convertResult(byInsuranceNumberResult)
                if (validationResult.status == PatientValidationResult.PATIENT_FOUND) {
                    validationResult
                } else {
                    logger.info {
                        "Foreigner was not found in ISIN by insurance number ${insuranceNumber}. Trying to found the " +
                        "patient by first name, last name and personal number "
                    }
                    val byPersonalNumberResult = isinService.getPatientByParameters(
                        firstName = firstName,
                        lastName = lastName,
                        personalNumber = insuranceNumber
                    )
                    logger.info {
                        "Data from ISIN for patient (foreigner) ${firstName}/${lastName}/${insuranceNumber}: " +
                        "result=${byPersonalNumberResult.result}, resultMessage=${byPersonalNumberResult.resultMessage}, patientId=${byPersonalNumberResult.patientId}."
                    }
                    convertResult(byPersonalNumberResult)
                }
            } else {
                logger.error {
                    "Both personal and insurance numbers are not set for patient ${firstName} ${lastName}. " +
                    "This should not happen. Skipping ISIN validation."
                }
                IsinValidationResultDto(status = PatientValidationResult.WAS_NOT_VERIFIED)
            }
        }.onSuccess {
            logger.info { "Data retrieval from ISIN - success." }
        }.onFailure {
            logger.warn { "Data retrieval from ISIN - failure." }
            // TODO #287 we think that this is the place which produces stack overflow
            val wrappingException =
                Exception("An exception ${it.javaClass.canonicalName} was thrown! - ${it.message}\n${it.stackTraceToString()}")
            logger.error(wrappingException) {
                "Getting data from ISIN server failed for patient ${firstName} ${lastName}, " +
                "personalNumber=${personalNumber}, insuranceNumber=${insuranceNumber}"
            }
        }.getOrNull() ?: IsinValidationResultDto(status = PatientValidationResult.WAS_NOT_VERIFIED)

    }

    private fun convertResult(result: IsinGetPatientByParametersResultDto): IsinValidationResultDto =
        when (result.result) {
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
