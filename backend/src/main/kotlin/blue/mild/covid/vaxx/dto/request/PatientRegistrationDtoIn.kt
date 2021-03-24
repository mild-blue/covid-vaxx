package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto


data class PatientRegistrationDtoIn(
    val firstName: String,
    val lastName: String,
    val zipCode: Int,
    val district: String,
    val personalNumber: String,
    val phoneNumber: PhoneNumberDtoIn,
    val email: String,
    val insuranceCompany: InsuranceCompany,
    val answers: List<AnswerDto>,
    val confirmation: ConfirmationDtoIn
)
