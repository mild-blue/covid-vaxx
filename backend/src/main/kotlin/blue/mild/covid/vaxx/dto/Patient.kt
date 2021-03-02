package blue.mild.covid.vaxx.dto

import java.util.UUID

data class PatientDto(
    val id: UUID,
    val name: String
)

data class NewPatientDto(
    val name: String
)
