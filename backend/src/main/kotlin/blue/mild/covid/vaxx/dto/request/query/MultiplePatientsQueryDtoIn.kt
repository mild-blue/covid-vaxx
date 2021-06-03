package blue.mild.covid.vaxx.dto.request.query

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class MultiplePatientsQueryDtoIn(
    @QueryParam("Filter by email.") val email: String?,
    @QueryParam("Filter by phone number.") val phoneNumber: String?,
    @QueryParam("Filter by the fact if the patient was vaccinated or not.") val vaccinated: Boolean?
) {
    override fun toString(): String =
        listOfNotNull(
            email?.let { "email=$it" },
            phoneNumber?.let { "phoneNumber=$it" },
            vaccinated?.let { "vaccinated=$it" },
        ).joinToString(", ", prefix = "query(", postfix = ")")
}
