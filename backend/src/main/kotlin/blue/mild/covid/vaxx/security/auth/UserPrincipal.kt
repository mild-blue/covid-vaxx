package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.UserRole
import io.ktor.auth.Principal
import java.util.UUID

/**
 * Principal for the user.
 */
sealed class UserPrincipal : Principal

/**
 * Represents user that passed Captcha but is not registered.
 */
object PatientPrincipal : UserPrincipal()

/**
 * Registered user.
 */
data class RegisteredUserPrincipal(
    val userId: UUID,
    val userRole: UserRole
) : UserPrincipal()
