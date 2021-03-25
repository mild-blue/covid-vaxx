package blue.mild.covid.vaxx.dto.request.query

import blue.mild.covid.vaxx.dao.model.EntityId
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class PatientIdQueryDtoIn(
    @QueryParam("Patients ID")
    val id: EntityId
)
