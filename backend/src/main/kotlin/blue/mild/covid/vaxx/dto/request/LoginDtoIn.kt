package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId

data class LoginDtoIn(
    val email: String,
    val password: String,
    val nurseId: EntityId? = null,
    val vaccineSerialNumber: String
)
