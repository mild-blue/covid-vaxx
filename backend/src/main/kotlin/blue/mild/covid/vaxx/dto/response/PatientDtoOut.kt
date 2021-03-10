package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import java.time.Instant
import java.util.UUID

data class PatientDtoOut(
    val id: UUID,
    val created: Instant,
    val updated: Instant,
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val registrationEmailSentOn: Instant?,
    val vaccinatedOn: Instant?,
    val insuranceCompany: InsuranceCompany,
    val answers: List<AnswerDto>
)
