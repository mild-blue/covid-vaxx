package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.InsuranceCompany


data class PatientRegistrationDtoIn(
    val firstName: String,
    val lastName: String,
    val zipCode: Int,
    val district: String,
    val personalNumber: String,
    val phoneNumber: PhoneNumberDtoIn,
    val email: String,
    val insuranceCompany: InsuranceCompany,
    val indication: String? = null,
    val answers: List<AnswerDtoIn>,
    val confirmation: ConfirmationDtoIn
)
