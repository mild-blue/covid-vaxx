package blue.mild.covid.vaxx.dto.response

import java.util.UUID

data class QuestionDtoOut(
    val id: UUID,
    val placeholder: String,
    val cs: String,
    val eng: String
)
