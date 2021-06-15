package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class DataCorrectnessConfirmationDtoOut(
    val id: EntityId,
    val dataAreCorrect: Boolean,
    val exportedToIsinOn: Instant?,
    val notes: String?
)
