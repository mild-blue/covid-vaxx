package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.User
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.security.auth.CredentialsMismatchException
import blue.mild.covid.vaxx.security.auth.RegisteredUserPrincipal
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid

class UserService(
    private val idProvider: EntityIdProvider,
    private val passwordHashProvider: PasswordHashProvider
) {

    suspend fun verifyCredentials(login: LoginDtoIn) = newSuspendedTransaction {
        val userRow = User
            .slice(User.id, User.passwordHash, User.role)
            .select { User.username eq login.username }
            .singleOrNull() ?: throw CredentialsMismatchException()

        val authorized = passwordHashProvider.verifyPassword(password = login.password, passwordHash = userRow[User.passwordHash])
        if (authorized) {
            RegisteredUserPrincipal(
                userId = userRow[User.id].toUuid(),
                userRole = userRow[User.role]
            )
        } else {
            throw CredentialsMismatchException()
        }
    }

    suspend fun registerUser(registration: UserRegistrationDtoIn) = newSuspendedTransaction {
        val (entityId, stringId) = idProvider.generateId()
        User.insert {
            it[id] = stringId
            it[username] = registration.username.trim()
            it[passwordHash] = passwordHashProvider.hashPassword(registration.password)
            it[role] = registration.role
        }
        UserRegisteredDtoOut(entityId)
    }
}
