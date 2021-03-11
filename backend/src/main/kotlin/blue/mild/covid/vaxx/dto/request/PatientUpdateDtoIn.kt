package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import java.time.Instant

data class PatientUpdateDtoIn(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val personalNumber: String? = null,
    val email: String? = null,
    val insuranceCompany: InsuranceCompany? = null,
    val vaccinatedOn: Instant? = null,
    val answers: List<AnswerDto>? = null,
)
