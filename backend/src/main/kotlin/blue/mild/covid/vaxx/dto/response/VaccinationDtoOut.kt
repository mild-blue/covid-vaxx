package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class VaccinationDtoOut(
    val id: EntityId,
    val vaccinatedOn: Instant
)
