package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId

data class LocationDtoOut(
    val id: EntityId,
    val address: String,
    val zipCode: Int,
    val district: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val slots: List<VaccinationSlotDtoOut>
)
