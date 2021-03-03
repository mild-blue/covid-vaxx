package blue.mild.covid.vaxx.dto

import java.util.UUID

data class PatientDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val answers: List<AnswerDto>
)
