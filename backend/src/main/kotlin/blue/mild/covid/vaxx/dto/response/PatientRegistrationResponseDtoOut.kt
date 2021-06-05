package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId

data class PatientRegistrationResponseDtoOut(
    val patientId: EntityId,
    val slot: VaccinationSlotDtoOut,
    val location: LocationDtoOut
)
