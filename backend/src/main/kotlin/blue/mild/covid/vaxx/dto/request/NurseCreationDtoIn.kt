package blue.mild.covid.vaxx.dto.request

data class NurseCreationDtoIn(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)
