package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import java.time.Instant

data class LocationDtoOut(
    val id: EntityId,
    val address: String,
    val zipCode: Int,
    val district: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val note: String? = null,
    val slots: List<VaccinationSlotDtoOut>
)
