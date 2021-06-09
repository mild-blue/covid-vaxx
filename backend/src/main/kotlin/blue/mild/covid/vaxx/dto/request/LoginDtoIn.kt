package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.LocalDate

data class LoginDtoIn(
    val credentials: CredentialsDtoIn,
    val nurseId: EntityId? = null,
    val vaccineSerialNumber: String,
    val vaccineExpiration: LocalDate
)
