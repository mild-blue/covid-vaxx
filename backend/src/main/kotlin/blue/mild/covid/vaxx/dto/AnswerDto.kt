package blue.mild.covid.vaxx.dto

import java.util.UUID

data class AnswerDto(
    val questionId: UUID,
    val value: Boolean
)
