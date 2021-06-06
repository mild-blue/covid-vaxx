package blue.mild.covid.vaxx.dto.internal

sealed interface PatientValidationResultDto {
    val status: PatientValidationResult
    val patientId: String?
}

data class IsinValidationResultDto(
    override val status: PatientValidationResult,
    override val patientId: String? = null
) : PatientValidationResultDto
