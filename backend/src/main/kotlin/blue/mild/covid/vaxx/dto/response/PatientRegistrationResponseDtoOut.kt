package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class PatientRegistrationResponseDtoOut(
    val email: String,
    val slot: VaccinationSlot
) {
    data class VaccinationSlot(
        val locationId: EntityId,
        val queue: Int,
        val from: Instant,
        val to: Instant
    )
}
