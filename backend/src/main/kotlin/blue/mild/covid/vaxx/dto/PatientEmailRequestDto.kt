package blue.mild.covid.vaxx.dto

import java.util.UUID

data class PatientEmailRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val patientId: UUID
)
