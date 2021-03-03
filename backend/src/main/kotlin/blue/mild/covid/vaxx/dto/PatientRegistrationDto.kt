package blue.mild.covid.vaxx.dto


data class PatientRegistrationDto(
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val answers: List<AnswerDto>
)
