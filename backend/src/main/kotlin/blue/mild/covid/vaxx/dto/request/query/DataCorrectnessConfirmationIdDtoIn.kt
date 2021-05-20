package blue.mild.covid.vaxx.dto.request.query

import blue.mild.covid.vaxx.dao.model.EntityId
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam

@Path("{id}")
data class DataCorrectnessConfirmationIdDtoIn(
    @PathParam("Data correctness confirmation ID")
    val id: EntityId
)
