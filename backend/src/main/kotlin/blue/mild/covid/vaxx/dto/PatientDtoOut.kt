package blue.mild.covid.vaxx.dto

import blue.mild.covid.vaxx.dao.InsuranceCompany
import java.time.Instant
import java.util.UUID

data class PatientDtoOut(
    val id: UUID,
    val created: Instant,
    val updated: Instant?,
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val insuranceCompany: InsuranceCompany,
    val answers: List<AnswerDto>
)
