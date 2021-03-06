package blue.mild.covid.vaxx.integrations

data class PatientRegistrationDtoInForTests(
    val firstName: Any?,
    val lastName: Any?,
    val personalNumber: Any?,
    val phoneNumber: Any?,
    val email: Any?,
    val insuranceCompany: Any?,
    val answers: Any?,
    val confirmation: Any?
)

data class AnswerDtoInForTest(
    val questionId: Any?,
    val value: Any?
)

data class ConfirmationDtoInForTest(
    val healthStateDisclosureConfirmation: Any? = null,
    val covid19VaccinationAgreement: Any? = null,
    val gdprAgreement: Any? = null

)

data class WrongObjectDtoInForTest(
    val test: Any? = null
)

data class LoginUserDtoInForTest(
    val username: Any,
    val password: Any
)
