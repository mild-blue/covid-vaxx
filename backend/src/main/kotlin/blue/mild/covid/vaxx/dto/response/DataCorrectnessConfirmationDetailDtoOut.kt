package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class DataCorrectnessConfirmationDetailDtoOut(
    val id: EntityId,
    val checked: Instant,
    val doctor: PersonnelDtoOut,
    val nurse: PersonnelDtoOut?,
    val dataAreCorrect: Boolean,
    val notes: String?
)
