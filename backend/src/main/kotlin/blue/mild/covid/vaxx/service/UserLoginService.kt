package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.auth.RegisteredUserPrincipal
import blue.mild.covid.vaxx.dao.UserRole
import java.util.UUID

class UserLoginService {

    fun verifyCredentials(username: String, password: String): RegisteredUserPrincipal =
        // TODO implement this
        RegisteredUserPrincipal(UUID.randomUUID(), UserRole.ADMIN)
}
