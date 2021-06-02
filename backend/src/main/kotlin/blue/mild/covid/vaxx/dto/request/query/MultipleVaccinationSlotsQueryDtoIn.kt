package blue.mild.covid.vaxx.dto.request.query

import blue.mild.covid.vaxx.dao.model.EntityId
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import java.time.Instant

enum class VaccinationSlotStatus {
    ALL,
    ONLY_FREE,
    ONLY_OCCUPIED,
}

data class MultipleVaccinationSlotsQueryDtoIn(
    @QueryParam("Filter by slot id.") val id: EntityId?,
    @QueryParam("Filter by location id.") val locationId: EntityId?,
    @QueryParam("Filter by patient id.") val patientId: EntityId?,
    @QueryParam("Filter with from greater or equal to.") val from: Instant?,
    @QueryParam("Filter with to lower or equal to.") val to: Instant?,
    @QueryParam("Filter only free slots?.") val status: VaccinationSlotStatus?,
) {
    override fun toString(): String =
        listOfNotNull(
            id?.let { "id=$it" },
            locationId?.let { "locationId=$it" },
            patientId?.let { "patientId=$it" },
            from?.let { "from=$it" },
            to?.let { "to=$it" },
            status?.let { "status=$it" },
        ).joinToString(", ", prefix = "query(", postfix = ")")
}

data class BookVaccinationSlotsQueryDtoIn(
    @QueryParam("Filter by slot id.") val id: EntityId?,
    @QueryParam("Filter by location id.") val locationId: EntityId?,
    @QueryParam("Filter with from greater or equal to.") val from: Instant?,
    @QueryParam("Filter with to lower or equal to.") val to: Instant?,
) {
    override fun toString(): String =
        listOfNotNull(
            id?.let { "id=$it" },
            locationId?.let { "locationId=$it" },
            from?.let { "from=$it" },
            to?.let { "to=$it" },
        ).joinToString(", ", prefix = "query(", postfix = ")")
}
