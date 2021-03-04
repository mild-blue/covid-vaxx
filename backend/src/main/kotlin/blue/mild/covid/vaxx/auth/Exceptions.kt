package blue.mild.covid.vaxx.auth

sealed class AuthorizationException(override val message: String) : Exception(message)

class GenericAuthException(message: String): AuthorizationException(message)

class InsufficientRightsException(message: String = ""): AuthorizationException(message)

class InvalidJwtException(message: String = ""): AuthorizationException(message)

class CredentialsMismatchException(message: String = ""): AuthorizationException(message)

