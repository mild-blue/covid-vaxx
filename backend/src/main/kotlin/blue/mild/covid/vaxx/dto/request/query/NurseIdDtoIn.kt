package blue.mild.covid.vaxx.dto.request.query

import blue.mild.covid.vaxx.dao.model.EntityId
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam

@Path("{id}")
data class NurseIdDtoIn(@PathParam("Nurse ID") val id: EntityId)
