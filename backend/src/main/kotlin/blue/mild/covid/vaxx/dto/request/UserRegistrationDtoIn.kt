package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.UserRole

data class UserRegistrationDtoIn(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
)
