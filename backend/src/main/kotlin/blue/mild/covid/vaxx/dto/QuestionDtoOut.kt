package blue.mild.covid.vaxx.dto

import java.util.UUID

data class QuestionDtoOut(
    val id: UUID,
    val placeholder: String,
    val cs: String,
    val eng: String
)
