package blue.mild.covid.vaxx.dto.request

data class ConfirmationDtoIn(
    val healthStateDisclosureConfirmation: Boolean,
    val covid19VaccinationAgreement: Boolean
)
