package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.Vaccinations
import blue.mild.covid.vaxx.dao.repository.DataCorrectnessRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.DataCorrectnessDtoIn
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDetailDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import dev.forst.katlib.TimeProvider
import dev.forst.katlib.whenFalse
import java.time.Instant

class DataCorrectnessService(
    private val dataCorrectnessRepository: DataCorrectnessRepository,
    private val instantTimeProvider: TimeProvider<Instant>
) {
    /**
     * Returns [DataCorrectnessConfirmationDetailDtoOut] if the patient's data was verified.
     */
    suspend fun getForPatient(patientId: EntityId): DataCorrectnessConfirmationDetailDtoOut =
        dataCorrectnessRepository.getForPatient(patientId)
            ?: throw entityNotFound<PatientDataCorrectnessConfirmation>(PatientDataCorrectnessConfirmation::patientId, patientId)

    /**
     * Returns [DataCorrectnessConfirmationDetailDtoOut] if the data correctness verification id is found.
     */
    suspend fun get(id: EntityId): DataCorrectnessConfirmationDetailDtoOut =
        dataCorrectnessRepository.get(id)
            ?: throw entityNotFound<PatientDataCorrectnessConfirmation>(PatientDataCorrectnessConfirmation::id, id)

    /**
     * Store that the patient's data were verified.
     */
    suspend fun registerCorrectness(
        request: ContextAware.AuthorizedContext<DataCorrectnessDtoIn>,
        wasExportedToIsin: Boolean
    ): EntityId {
        // todo perform some validation

        val dataCorrectness = request.payload
        val principal = request.principal

        val exportedToIsinOn = if (wasExportedToIsin) {
            instantTimeProvider.now()
        } else {
            null
        }

        return dataCorrectnessRepository.registerCorrectness(
            patientId = dataCorrectness.patientId,
            userPerformedCheck = principal.userId,
            nurseId = principal.nurseId,
            dataAreCorrect = dataCorrectness.dataAreCorrect,
            notes = dataCorrectness.notes?.trim(),
            exportedToIsinOn = exportedToIsinOn
        )
    }

    /**
     * Register in database that [correctnessId] was exported to ISIN.
     */
    suspend fun exportedToIsin(correctnessId: EntityId, storedOn: Instant = instantTimeProvider.now()) {
        dataCorrectnessRepository.updateCorrectness(
            correctnessId = correctnessId,
            exportedToIsinOn = storedOn
        ).whenFalse { throw entityNotFound<Vaccinations>(PatientDataCorrectnessConfirmation::id, PatientDataCorrectnessConfirmation) }
    }
}
