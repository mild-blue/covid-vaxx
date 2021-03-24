package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.InsuranceCompany

data class PatientUpdateDtoIn(
    val firstName: String? = null,
    val lastName: String? = null,
    val zipCode: Int? = null,
    val district: String? = null,
    val phoneNumber: PhoneNumberDtoIn? = null,
    val personalNumber: String? = null,
    val email: String? = null,
    val insuranceCompany: InsuranceCompany? = null,
    val answers: List<AnswerDtoIn>? = null,
) {
    override fun toString(): String =
        listOfNotNull(
            firstName?.let { "firstName=$it" },
            lastName?.let { "lastName=$it" },
            zipCode?.let { "zipCode=$it" },
            district?.let { "district=$it" },
            phoneNumber?.let { "phoneNumber=${it.number}" },
            personalNumber?.let { "personalNumber=$it" },
            email?.let { "email=$it" },
            insuranceCompany?.let { "insuranceCompany=$it" },
            answers?.let { answers -> "answers=[${answers.joinToString(", ") { "${it.questionId}: ${it.value}" }}]" },
        ).joinToString(", ", prefix = "update(", postfix = ")")
}
