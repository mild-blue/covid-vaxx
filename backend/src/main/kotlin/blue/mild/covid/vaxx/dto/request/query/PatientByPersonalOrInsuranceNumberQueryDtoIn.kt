package blue.mild.covid.vaxx.dto.request.query

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class PatientByPersonalOrInsuranceNumberQueryDtoIn(
    @QueryParam("Patient personal number") val personalNumber: String?,
    @QueryParam("Patient insurance number") val insuranceNumber: String?
)
