package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut

@Suppress("MagicNumber")
data class PatientEmailRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val patientId: EntityId,
    val slot: VaccinationSlotDtoOut,
    val location: LocationDtoOut,
    val attemptLeft: Int = 10
)
