package blue.mild.covid.vaxx.dto.response

data class IsinJobDtoOut(
    val validatedPatientsSuccess: Int,
    val validatedPatientsErrors: Int,

    val checkedVaccinationsSuccess: Int,
    val checkedVaccinationsErrors: Int,

    val exportedPatientsInfoSuccess: Int,
    val exportedPatientsInfoErrors: Int,

    val exportedVaccinationsFirstDoseSuccess: Int,
    val exportedVaccinationsFirstDoseErrors: Int,

    val exportedVaccinationsSecondDoseSuccess: Int,
    val exportedVaccinationsSecondDoseErrors: Int
)
