package blue.mild.covid.vaxx.dto

import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn

data class PatientRegistrationDto(
    val registration: PatientRegistrationDtoIn,
    val remoteHost: String
)
