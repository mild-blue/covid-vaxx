package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto

data class PatientUpdateDtoIn(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val personalNumber: String? = null,
    val email: String? = null,
    val insuranceCompany: InsuranceCompany? = null,
    val answers: List<AnswerDto>? = null,
)
