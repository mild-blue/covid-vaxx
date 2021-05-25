package blue.mild.covid.vaxx.dto.request.query

import blue.mild.covid.vaxx.dao.model.EntityId
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

enum class VaccinationSlotStatus(val status: Int) {
    ALL(0),
    ONLY_FREE(1),
    ONLY_OCCUPIED(2),
}

data class MultipleVaccinationSlotsQueryDtoOut(
    @QueryParam("Filter by slot id.") val id: EntityId?,
    @QueryParam("Filter by location id.") val locationId: EntityId?,
    @QueryParam("Filter with from greater or equal to.") val fromMillis: Long?,
    @QueryParam("Filter with to lower or equal to.") val toMillis: Long?,
    @QueryParam("Filter only free slots?.") val status: VaccinationSlotStatus?,

) {
    override fun toString(): String =
        listOfNotNull(
            id?.let { "id=$it" },
            locationId?.let { "locationId=$it" },
            fromMillis?.let { "fromMillis=$it" },
            toMillis?.let { "toMillis=$it" },
            status?.let { "status=$it" },
        ).joinToString(", ", prefix = "query(", postfix = ")")
}
