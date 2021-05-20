package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId

data class DataCorrectnessConfirmationDtoOut(
    val id: EntityId,
    val dataAreCorrect: Boolean
)
