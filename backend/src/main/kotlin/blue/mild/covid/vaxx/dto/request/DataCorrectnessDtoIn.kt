package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId

data class DataCorrectnessDtoIn(
    val patientId: EntityId,
    val dataAreCorrect: Boolean,
    val notes: String?
)
