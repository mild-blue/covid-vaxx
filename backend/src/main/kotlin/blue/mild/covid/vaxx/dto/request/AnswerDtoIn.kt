package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId

data class AnswerDtoIn(
    val questionId: EntityId,
    val value: Boolean
)
