package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import io.ktor.auth.Principal
import java.time.LocalDate

/**
 * Registered user.
 */
data class UserPrincipal(
    val userId: EntityId,
    val userRole: UserRole,
    val vaccineSerialNumber: String,
    val vaccineExpiration: LocalDate,
    val nurseId: EntityId? = null,
) : Principal
