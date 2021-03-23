package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.EntityId

/**
 * Multiple authorization exceptions that can occur during auth phase of the application.
 */
sealed class AuthorizationException(override val message: String) : Exception(message)

class GenericAuthException(message: String) : AuthorizationException(message)

class InsufficientRightsException(message: String = "") : AuthorizationException(message)

class InvalidJwtException(message: String = "") : AuthorizationException(message)

class CredentialsMismatchException(message: String = "") : AuthorizationException(message)

class CaptchaFailedException(message: String = "") : AuthorizationException(message)

class NonExistingNurseException(nurseId: EntityId) : AuthorizationException("Nurse with id $nurseId does not exist!")
