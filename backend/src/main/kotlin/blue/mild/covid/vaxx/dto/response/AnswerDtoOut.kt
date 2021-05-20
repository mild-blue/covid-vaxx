package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId

data class AnswerDtoOut(
    val questionId: EntityId,
    val value: Boolean
)
