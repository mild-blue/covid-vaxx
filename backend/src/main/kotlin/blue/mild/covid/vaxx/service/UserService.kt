package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dao.repository.UserRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.security.auth.AuthorizationException
import blue.mild.covid.vaxx.security.auth.CredentialsMismatchException
import blue.mild.covid.vaxx.security.auth.NonExistingNurseException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import dev.forst.katlib.whenFalse
import dev.forst.katlib.whenTrue
import mu.KLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.Locale

class UserService(
    private val userRepository: UserRepository,
    private val passwordHashProvider: PasswordHashProvider
) {

    private companion object : KLogging()

    /**
     * Verifies, that the credentials exist and that the password match.
     * This is basically light login for a single request that does not produce token.
     *
     * Throws [CredentialsMismatchException] if they do not.
     */
    suspend fun verifyCredentials(email: String, password: String) {
        val passwordHash = userRepository.viewByEmail(email.trim().lowercase(Locale.getDefault())) {
            it[passwordHash]
        } ?: throw CredentialsMismatchException()

        passwordHashProvider.verifyPassword(password, passwordHash = passwordHash)
            .whenFalse { throw CredentialsMismatchException() }
    }

    /**
     * Verifies credentials and creates principal. If user does not exist
     * or supplied wrong password, throws [AuthorizationException].
     */
    suspend fun createPrincipal(request: ContextAware<LoginDtoIn>): UserPrincipal = newSuspendedTransaction {
        val login = request.payload
        val credentials = login.credentials
        // verify existing user
        val (userId, passwordHash, role) = userRepository.viewByEmail(credentials.email.trim().lowercase(Locale.getDefault())) {
            Triple(it[id], it[passwordHash], it[role])
        } ?: loginFailed(request, null) { CredentialsMismatchException() }

        // verify passwords
        val passwordsMatch = passwordHashProvider.verifyPassword(credentials.password, passwordHash = passwordHash)
        if (!passwordsMatch) {
            loginFailed(request, userId) { CredentialsMismatchException() }
        }

        // verify that the selected nurse exist
        login.nurseId?.also { nurseId ->
            newSuspendedTransaction {
                Nurses.select { Nurses.id eq nurseId }.empty()
            }.whenTrue { loginFailed(request, userId, nurseExists = false) { NonExistingNurseException(nurseId) } }
        }

        // finally build principal
        UserPrincipal(
            userId = userId,
            userRole = role,
            vaccineSerialNumber = login.vaccineSerialNumber.trim(),
            vaccineExpiration = login.vaccineExpiration,
            nurseId = login.nurseId
        ).also {
            // log that the user had successful login
            userRepository.recordLogin(
                userId = it.userId,
                success = true,
                remoteHost = request.remoteHost,
                callId = request.callId,
                vaccineSerialNumber = it.vaccineSerialNumber,
                vaccineExpiration = it.vaccineExpiration,
                nurseId = it.nurseId
            )
        }
    }

    private suspend inline fun loginFailed(
        request: ContextAware<LoginDtoIn>,
        userId: EntityId?,
        nurseExists: Boolean = true,
        exception: () -> AuthorizationException
    ): Nothing {
        logger.warn { "Login failed for user ${request.payload.credentials.email}." }
        if (userId != null) {
            userRepository.recordLogin(
                userId = userId,
                success = false,
                remoteHost = request.remoteHost,
                callId = request.callId,
                vaccineSerialNumber = request.payload.vaccineSerialNumber.trim(),
                vaccineExpiration = request.payload.vaccineExpiration,
                nurseId = if (nurseExists) request.payload.nurseId else null
            )
        }
        throw exception()
    }

    /**
     * Creates new user registration.
     */
    suspend fun registerUser(registration: UserRegistrationDtoIn): UserRegisteredDtoOut =
        userRepository.saveUser(
            firstName = registration.firstName.trim(),
            lastName = registration.lastName.trim(),
            email = registration.email.trim(),
            passwordHash = passwordHashProvider.hashPassword(registration.password),
            role = registration.role
        ).let(::UserRegisteredDtoOut)
}
