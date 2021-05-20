package blue.mild.covid.vaxx.dto.request.query

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import java.time.Instant

data class SystemStatisticsFilterDtoIn(
    @QueryParam("Date FROM which should system provide statistics.")
    val from: Instant? = null,
    @QueryParam("Date TO which should system provide statistics.")
    val to: Instant? = null
)
