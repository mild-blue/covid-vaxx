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
) {
    override fun toString(): String =
        listOfNotNull(
            firstName?.let { "firstName=$it" },
            lastName?.let { "lastName=$it" },
            phoneNumber?.let { "phoneNumber=$it" },
            personalNumber?.let { "personalNumber=$it" },
            email?.let { "email=$it" },
            insuranceCompany?.let { "insuranceCompany=$it" },
            vaccinatedOn?.let { "vaccinatedOn=$it" },
            answers?.let { answers -> "answers=[${answers.joinToString(",") { "${it.questionId}: ${it.value}" }}]" },
        ).joinToString(", ", prefix = "update(", postfix = ")")
}
