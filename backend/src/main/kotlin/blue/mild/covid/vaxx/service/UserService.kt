package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.User
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.security.auth.CredentialsMismatchException
import blue.mild.covid.vaxx.security.auth.RegisteredUserPrincipal
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid
import java.util.UUID

class UserService(private val passwordHashProvider: PasswordHashProvider) {

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

    suspend fun registerUser(registration: UserRegistrationDtoIn): Unit = newSuspendedTransaction {
        User.insert {
            it[id] = UUID.randomUUID().toString()
            it[username] = registration.username
            it[passwordHash] = passwordHashProvider.hashPassword(registration.password)
            it[role] = registration.role
        }
    }
}
