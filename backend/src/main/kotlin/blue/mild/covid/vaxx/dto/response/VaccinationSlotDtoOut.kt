package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

data class VaccinationSlotDtoOut(
    val id: EntityId,
    val locationId: EntityId,
    val patientId: EntityId?,
    val queue: Int,
    val from: Instant,
    val to: Instant
) {
    private fun getFromWithOffset(): ZoneOffset? {
        return ZoneId.of("Europe/Prague").rules.getOffset(this.from)
    }

    fun date(): String {
        return "${this.from.atOffset(getFromWithOffset()).dayOfMonth}." +
                "${this.from.atOffset(getFromWithOffset()).monthValue}." +
                "${this.from.atOffset(getFromWithOffset()).year}"
    }

    fun time(): String {
        return "${this.from.atOffset(getFromWithOffset()).hour}:" +
                "${this.from.atOffset(getFromWithOffset()).minute}"
    }
}
