package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.UserRole
import io.ktor.auth.Principal
import java.util.UUID

/**
 * Registered user.
 */
data class UserPrincipal(
    val userId: UUID,
    val userRole: UserRole
) : Principal
