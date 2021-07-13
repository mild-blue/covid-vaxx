package blue.mild.covid.vaxx.dto.request

data class IsinJobDtoIn(
    val validatePatients: Boolean = false,
    val checkVaccinations: Boolean = false,
    val exportPatientsInfo: Boolean = false,
    val exportVaccinationsFirstDose: Boolean = false,
    val exportVaccinationsSecondDose: Boolean = false,

    val patientsOffset: Int = 0,
    val patientsCount: Int? = null
)
