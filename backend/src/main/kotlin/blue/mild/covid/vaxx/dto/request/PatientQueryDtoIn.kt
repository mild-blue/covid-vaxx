package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import java.util.UUID

data class PatientQueryDtoIn(
    @QueryParam("Patient ID") val id: UUID?,
    @QueryParam("Patient personal number") val personalNumber: String?,
    @QueryParam("Patient email") val email: String?
)
