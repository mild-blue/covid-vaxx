package blue.mild.covid.vaxx.dto.internal

data class IsinValidationResultDto(
    val status: IsinValidationResultStatus,
    val patientId: String? = null
)
