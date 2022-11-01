package blue.mild.covid.vaxx.dto.internal

data class IsinGetPatientByParametersResultDto(
    val result: String,
    val resultMessage: String?,
    val patientId: String?
)
