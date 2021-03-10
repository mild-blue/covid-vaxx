package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class MultiplePatientsQueryDtoOut(
    @QueryParam("Filter by email.") val email: String?,
    @QueryParam("Filter by phone number.") val phoneNumber: String?,
    @QueryParam("Filter by the fact if the patient was vaccinated or not.") val vaccinated: Boolean?
)
