package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId

data class PersonnelDtoOut(
    val id: EntityId,
    val firstName: String,
    val lastName: String,
    val email: String
)
