package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.properties.description.Description

data class PhoneNumberDtoIn(
    @Description("Phone number without country prefix.")
    val number: String,
    @Description("Country code.")
    val countryCode: String
)
