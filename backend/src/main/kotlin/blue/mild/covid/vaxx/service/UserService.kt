package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.repository.UserRepository
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.security.auth.CredentialsMismatchException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import mu.KLogging

class UserService(
    private val userRepository: UserRepository,
    private val idProvider: EntityIdProvider,
    private val passwordHashProvider: PasswordHashProvider
) {

    private companion object : KLogging()

    /**
     * Verifies credentials and creates principal. If user does not exist
     * or supplied wrong password, throws [CredentialsMismatchException].
     */
    suspend fun verifyCredentials(login: LoginDtoIn): UserPrincipal {
        val (id, passwordHash, role) = userRepository.viewByUsername(login.username) {
            Triple(it[id], it[passwordHash], it[role])
        } ?: throw CredentialsMismatchException()

        val passwordsMatch = passwordHashProvider.verifyPassword(login.password, passwordHash = passwordHash)
        if (!passwordsMatch) {
            throw CredentialsMismatchException()
        }

        return UserPrincipal(
            userId = id,
            userRole = role
        )
    }

    /**
     * Creates new user registration.
     */
    suspend fun registerUser(registration: UserRegistrationDtoIn): UserRegisteredDtoOut =
        userRepository.saveUser(
            id = idProvider.generateId(),
            username = registration.username.trim(),
            passwordHash = passwordHashProvider.hashPassword(registration.password),
            role = registration.role
        ).let(::UserRegisteredDtoOut)
}
