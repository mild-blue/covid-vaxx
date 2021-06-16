package blue.mild.covid.vaxx.dto.request

data class IsinJobDtoIn(
    val validatePatients: Boolean = false,
    val exportPatientsInfo: Boolean = false,
    val exportVaccinations: Boolean = false,

    val patientsOffset: Int = 0,
    val patientsCount: Int? = null
)
