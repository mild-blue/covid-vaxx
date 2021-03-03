package blue.mild.covid.vaxx.dto


data class PatientRegistrationDtoIn(
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val answers: List<AnswerDto>,
    val confirmation: ConfirmationDtoIn
)
