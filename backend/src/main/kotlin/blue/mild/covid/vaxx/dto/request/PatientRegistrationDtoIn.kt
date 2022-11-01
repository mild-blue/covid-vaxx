package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.InsuranceCompany

interface PatientBasicInfoDto {
    val firstName: String
    val lastName: String
    val personalNumber: String?
}

data class PatientRegistrationDtoIn(
    override val firstName: String,
    override val lastName: String,
    val zipCode: Int,
    val district: String,
    override val personalNumber: String?,
    val insuranceNumber: String?,
    val phoneNumber: PhoneNumberDtoIn,
    val email: String,
    val insuranceCompany: InsuranceCompany,
    val indication: String? = null,
    val answers: List<AnswerDtoIn>,
    val confirmation: ConfirmationDtoIn
) : PatientBasicInfoDto
