package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Vaccinations
import blue.mild.covid.vaxx.dao.repository.VaccinationRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.VaccinationDtoIn
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import pw.forst.katlib.TimeProvider
import pw.forst.katlib.whenFalse
import java.time.Instant

class VaccinationService(
    private val vaccinationRepository: VaccinationRepository,
    private val instantTimeProvider: TimeProvider<Instant>
) {
    /**
     * Returns [VaccinationDetailDtoOut] if the patient was vaccinated.
     */
    suspend fun getForPatient(patientId: EntityId, doseNumber: Int): VaccinationDetailDtoOut =
        vaccinationRepository.getForPatient(patientId, doseNumber)
            ?: throw entityNotFound<Vaccinations>(Vaccinations::patientId, patientId)

    /**
     * Returns [VaccinationDetailDtoOut] if the vaccination id is found.
     */
    suspend fun get(id: EntityId): VaccinationDetailDtoOut =
        vaccinationRepository.get(id)
            ?: throw entityNotFound<Vaccinations>(Vaccinations::id, id)

    /**
     * Register in database that [vaccinationId] was exported to ISIN.
     */
    suspend fun exportedToIsin(vaccinationId: EntityId, storedOn: Instant = instantTimeProvider.now()) {
        vaccinationRepository.updateVaccination(
            vaccinationId = vaccinationId,
            exportedToIsinOn = storedOn
        ).whenFalse { throw entityNotFound<Vaccinations>(Vaccinations::id, vaccinationId) }
    }

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
            vaccineSerialNumber = principal.vaccineSerialNumber.trim(),
            vaccineExpiration = principal.vaccineExpiration,
            userPerformingVaccination = principal.userId,
            nurseId = principal.nurseId,
            notes = vaxx.notes?.trim(),
            doseNumber = vaxx.doseNumber
        )
    }
}
