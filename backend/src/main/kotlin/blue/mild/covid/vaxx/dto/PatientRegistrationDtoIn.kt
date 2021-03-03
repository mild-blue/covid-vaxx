package blue.mild.covid.vaxx.dto

import blue.mild.covid.vaxx.dao.InsuranceCompany


data class PatientRegistrationDtoIn(
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    val phoneNumber: String,
    val email: String,
    val insuranceCompany: InsuranceCompany,
    val answers: List<AnswerDto>,
    val confirmation: ConfirmationDtoIn
)
