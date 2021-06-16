package blue.mild.covid.vaxx.dto.response

data class IsinJobDtoOut(
    val validatedPatientsSuccess: Int,
    val validatedPatientsErrors: Int,

    val exportedPatientsInfoSuccess: Int,
    val exportedPatientsInfoErrors: Int,

    val exportedVaccinationsSuccess: Int,
    val exportedVaccinationsErrors: Int
)
