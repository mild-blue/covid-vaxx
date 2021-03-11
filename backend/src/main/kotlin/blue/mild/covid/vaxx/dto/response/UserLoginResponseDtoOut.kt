package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.UserRole

data class UserLoginResponseDtoOut(val token: String, val role: UserRole)
