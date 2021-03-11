package blue.mild.covid.vaxx.error

data class ErrorResponseDto(
    val message: String,
    val requestId: String?
)
