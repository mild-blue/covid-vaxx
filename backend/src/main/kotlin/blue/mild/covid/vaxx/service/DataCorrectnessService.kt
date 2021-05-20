package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.repository.DataCorrectnessRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.DataCorrectnessDtoIn
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDetailDtoOut
import blue.mild.covid.vaxx.error.entityNotFound

class DataCorrectnessService(
    private val dataCorrectnessRepository: DataCorrectnessRepository
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
    suspend fun registerCorrectness(request: ContextAware.AuthorizedContext<DataCorrectnessDtoIn>): EntityId {
        // todo perform some validation

        val dataCorrectness = request.payload
        val principal = request.principal

        return dataCorrectnessRepository.registerCorrectness(
            patientId = dataCorrectness.patientId,
            userPerformedCheck = principal.userId,
            nurseId = principal.nurseId,
            dataAreCorrect = dataCorrectness.dataAreCorrect,
            notes = dataCorrectness.notes?.trim()
        )
    }
}
