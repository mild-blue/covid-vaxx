package blue.mild.covid.vaxx.dto

data class ConfirmationDtoIn(
    val healthStateDisclosureConfirmation: Boolean,
    val covid19VaccinationAgreement: Boolean
)
