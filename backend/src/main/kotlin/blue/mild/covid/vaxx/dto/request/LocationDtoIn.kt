package blue.mild.covid.vaxx.dto.request

data class LocationDtoIn(
    val address: String,
    val zipCode: Int,
    val district: String,
    val phoneNumber: PhoneNumberDtoIn? = null,
    val email: String? = null,
    val note: String? = null
)
