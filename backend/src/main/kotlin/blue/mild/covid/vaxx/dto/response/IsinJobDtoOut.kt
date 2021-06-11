package blue.mild.covid.vaxx.dto.response

data class IsinJobDtoOut(
    var validatedPatientsSuccess: Int = 0,
    var validatedPatientsErrors: Int = 0,

    var exportedPatientsInfoSuccess: Int = 0,
    var exportedPatientsInfoErrors: Int = 0,

    var exportedVaccinationsSuccess: Int = 0,
    var exportedVaccinationsErrors: Int = 0
)
