package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId

data class LoginDtoIn(
    val credentials: CredentialsDtoIn,
    val nurseId: EntityId? = null,
    val vaccineSerialNumber: String
)
