package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import java.time.Instant

data class PatientDtoOut(
    val id: EntityId,
    val created: Instant,
    val updated: Instant,
    val firstName: String,
    val lastName: String,
    val zipCode: Int,
    val district: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val registrationEmailSentOn: Instant?,
    val insuranceCompany: InsuranceCompany,
    val answers: List<AnswerDto>
)
