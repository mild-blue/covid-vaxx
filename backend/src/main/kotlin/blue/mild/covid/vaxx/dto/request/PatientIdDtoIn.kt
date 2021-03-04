package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import java.util.UUID

@Path("{id}")
data class PatientIdDtoIn(@PathParam("Patient ID") val id: UUID)
