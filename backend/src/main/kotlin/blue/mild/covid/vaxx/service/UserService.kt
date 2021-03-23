package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurse
import blue.mild.covid.vaxx.dao.repository.UserRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.security.auth.AuthorizationException
import blue.mild.covid.vaxx.security.auth.CredentialsMismatchException
import blue.mild.covid.vaxx.security.auth.NonExistingNurseException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import mu.KLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.whenTrue

class UserService(
    private val userRepository: UserRepository,
    private val passwordHashProvider: PasswordHashProvider
) {

    private companion object : KLogging()

    /**
     * Verifies credentials and creates principal. If user does not exist
     * or supplied wrong password, throws [AuthorizationException].
     */
    suspend fun verifyCredentials(request: ContextAware<LoginDtoIn>): UserPrincipal = newSuspendedTransaction {
        val login = request.payload
        // verify existing user
        val (userId, passwordHash, role) = userRepository.viewByEmail(login.email) {
            Triple(it[id], it[passwordHash], it[role])
        } ?: loginFailed(request, null) { CredentialsMismatchException() }

        // verify passwords
        val passwordsMatch = passwordHashProvider.verifyPassword(login.password, passwordHash = passwordHash)
        if (!passwordsMatch) {
            loginFailed(request, userId) { CredentialsMismatchException() }
        }

        // verify that the selected nurse exist
        login.nurseId?.also { nurseId ->
            newSuspendedTransaction {
                Nurse.select { Nurse.id eq nurseId }.empty()
            }.whenTrue { loginFailed(request, userId) { NonExistingNurseException(nurseId) } }
        }

        // finally build principal
        UserPrincipal(
            userId = userId,
            userRole = role,
            vaccineSerialNumber = login.vaccineSerialNumber.trim(),
            nurseId = login.nurseId
        ).also {
            // log that the user had successful login
            userRepository.recordLogin(
                userId = it.userId,
                success = true,
                remoteHost = request.remoteHost,
                callId = request.callId,
                vaccineSerialNumber = it.vaccineSerialNumber,
                nurseId = it.nurseId
            )
        }
    }

    private suspend fun loginFailed(
        request: ContextAware<LoginDtoIn>,
        userId: EntityId?,
        exception: () -> AuthorizationException
    ): Nothing {
        logger.warn { "Login failed for user ${request.payload.email}." }
        if (userId != null) {
            userRepository.recordLogin(
                userId = userId,
                success = false,
                remoteHost = request.remoteHost,
                callId = request.callId,
                vaccineSerialNumber = request.payload.vaccineSerialNumber.trim(),
                nurseId = request.payload.nurseId
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
