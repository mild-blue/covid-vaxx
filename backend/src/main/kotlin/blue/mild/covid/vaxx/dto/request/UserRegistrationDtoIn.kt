package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.UserRole

data class UserRegistrationDtoIn(
    val username: String,
    val password: String,
    val role: UserRole
)
