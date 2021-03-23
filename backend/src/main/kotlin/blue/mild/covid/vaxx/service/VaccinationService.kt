package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Vaccination
import blue.mild.covid.vaxx.dao.repository.VaccinationRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.VaccinationDtoIn
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import blue.mild.covid.vaxx.error.entityNotFound

class VaccinationService(
    private val vaccinationRepository: VaccinationRepository
) {
    /**
     * Returns [VaccinationDetailDtoOut] if the patient was vaccinated.
     */
    suspend fun getForPatient(patientId: EntityId): VaccinationDetailDtoOut =
        vaccinationRepository.getForPatient(patientId)
            ?: throw entityNotFound<Vaccination>(Vaccination::patientId, patientId)

    /**
     * Returns [VaccinationDetailDtoOut] if the vaccination id is found.
     */
    suspend fun get(id: EntityId): VaccinationDetailDtoOut =
        vaccinationRepository.get(id)
            ?: throw entityNotFound<Vaccination>(Vaccination::id, id)

    /**
     * Creates new vaccination.
     */
    suspend fun addVaccination(request: ContextAware.AuthorizedContext<VaccinationDtoIn>): EntityId {
        // todo perform some validation

        val vaxx = request.payload
        val principal = request.principal

        return vaccinationRepository.addVaccination(
            patientId = vaxx.patientId,
            bodyPart = vaxx.bodyPart,
            vaccinatedOn = vaxx.vaccinatedOn,
            vaccineSerialNumber = principal.vaccineSerialNumber,
            userPerformingVaccination = principal.userId,
            nurseId = principal.nurseId,
            notes = vaxx.notes
        )
    }
}
