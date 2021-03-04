package blue.mild.covid.vaxx.auth

import blue.mild.covid.vaxx.dao.UserRole
import io.ktor.auth.Principal
import java.util.UUID

sealed class UserPrincipal : Principal

object PatientPrincipal : UserPrincipal()

data class RegisteredUserPrincipal(
    val userId: UUID,
    val userRole: UserRole
) : UserPrincipal()
