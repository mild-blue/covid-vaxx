package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class PatientByPersonalNumberQueryDtoIn(
    @QueryParam("Patient personal number") val personalNumber: String
)