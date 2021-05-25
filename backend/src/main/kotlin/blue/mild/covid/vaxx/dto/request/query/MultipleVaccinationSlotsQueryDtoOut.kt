package blue.mild.covid.vaxx.dto.request.query

import blue.mild.covid.vaxx.dao.model.EntityId
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class MultipleVaccinationSlotsQueryDtoOut(
    @QueryParam("Filter by location id.") val locationId: EntityId?,
    @QueryParam("Filter with from greater or equal to.") val fromMillis: Long?,
    @QueryParam("Filter with to lower or equal to.") val toMillis: Long?,
    @QueryParam("Filter only free slots?.") val onlyFree: Boolean?,

) {
    override fun toString(): String =
        listOfNotNull(
            locationId?.let { "locationId=$it" },
            fromMillis?.let { "fromMillis=$it" },
            toMillis?.let { "toMillis=$it" },
            onlyFree?.let { "onlyFree=$it" },
        ).joinToString(", ", prefix = "query(", postfix = ")")
}
