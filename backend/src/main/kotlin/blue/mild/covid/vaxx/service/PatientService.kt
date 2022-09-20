package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.DatabaseTypeLength
import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.error.EntityNotFoundException
import blue.mild.covid.vaxx.error.entityNotFound
import blue.mild.covid.vaxx.utils.formatPhoneNumber
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import blue.mild.covid.vaxx.utils.removeAllWhitespaces
import dev.forst.katlib.whenFalse
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import java.util.Locale
import java.util.UUID

class PatientService(
    private val patientRepository: PatientRepository,
    private val validationService: ValidationService
) {

    private companion object : KLogging()

    /**
     * Returns patient with given ID.
     */
    suspend fun getPatientById(patientId: EntityId): PatientDtoOut =
        patientRepository.getAndMapPatientsBy { Patients.id eq patientId }
            .singleOrNull()
            ?.withSortedAnswers() ?: throw entityNotFound<Patients>(Patients::id, patientId)

    /**
     * Returns single patient with given personal number or throws exception.
     */
    private suspend fun getPatientByPersonalNumber(patientPersonalNumber: String): PatientDtoOut? =
        patientRepository.getAndMapPatientsBy {
            Patients.personalNumber eq patientPersonalNumber.normalizePersonalNumber()
        }.singleOrNull()?.withSortedAnswers()

    /**
     * Returns single patient with given insurance number or throws exception.
     */
    private suspend fun getPatientByInsuranceNumber(patientInsuranceNumber: String): PatientDtoOut? =
        patientRepository.getAndMapPatientsBy {
            Patients.insuranceNumber eq patientInsuranceNumber.trim()
        }.singleOrNull()?.withSortedAnswers()

    /**
     * Returns single patient with given personal or insurance number or throws exception.
     */
    suspend fun getPatientByPersonalOrInsuranceNumber(patientPersonalOrInsuranceNumber: String): PatientDtoOut {
        // TODO theoretically, there could be a patient with an insurance number same as personal number of some other patient.
        //  In this case, we cannot find the patient with the personal number. https://github.com/mild-blue/covid-vaxx/issues/329
        var patient = getPatientByInsuranceNumber(patientPersonalOrInsuranceNumber)
        if (patient == null && patientPersonalOrInsuranceNumber.length <= DatabaseTypeLength.PERSONAL_NUMBER) {
            patient = getPatientByPersonalNumber(patientPersonalOrInsuranceNumber)
        }
        return patient ?: throw EntityNotFoundException(
            "Patient",
            "personal or insurance number",
            patientPersonalOrInsuranceNumber
        )
    }

    /**
     * Filters the database with the conjunction (and clause) of the given properties.
     */
    suspend fun getPatientsByConjunctionOf(
        email: String? = null,
        phoneNumber: String? = null,
        vaccinated: Boolean? = null,
        n: Int? = null,
        offset: Long = 0
    ): List<PatientDtoOut> =
        patientRepository.getAndMapPatientsBy(
            n = n,
            offset = offset
        ){
            Op.TRUE
                .andWithIfNotEmpty(email?.removeAllWhitespaces()?.lowercase(Locale.getDefault()), Patients.email)
                .andWithIfNotEmpty(phoneNumber?.removeAllWhitespaces(), Patients.phoneNumber)
                .let { query ->
                    vaccinated?.let {
                        query.and(
                            if (vaccinated) Patients.vaccination.isNotNull()
                            else Patients.vaccination.isNull()
                        )
                    } ?: query
                }
        }.sorted()

    /**
     * Updates patient with given change set.
     */
    suspend fun updatePatientWithChangeSet(patientId: UUID, changeSet: PatientUpdateDtoIn) {
        validationService.requireValidPatientUpdate(changeSet)

        patientRepository.updatePatientChangeSet(
            id = patientId,
            firstName = changeSet.firstName?.trim(),
            lastName = changeSet.lastName?.trim(),
            zipCode = changeSet.zipCode,
            district = changeSet.district?.trim(),
            phoneNumber = changeSet.phoneNumber?.formatPhoneNumber(),
            personalNumber = changeSet.personalNumber?.normalizePersonalNumber(),
            insuranceNumber = changeSet.insuranceNumber?.trim(),
            email = changeSet.email?.trim()?.lowercase(Locale.getDefault()),
            insuranceCompany = changeSet.insuranceCompany,
            indication = changeSet.indication?.trim(),
            answers = changeSet.answers?.associate { it.questionId to it.value }
        ).whenFalse { throw entityNotFound<Patients>(Patients::id, patientId) }
    }

    /**
     * Updates patient ISIN ready.
     */
    suspend fun updateIsinReady(patientId: UUID, isinReady: Boolean) {
        patientRepository.updatePatientChangeSet(
            id = patientId,
            isinReady = isinReady
        ).whenFalse { throw entityNotFound<Patients>(Patients::id, patientId) }
    }

    /**
     * Saves patient to the database and return its id.
     */
    suspend fun savePatient(registrationDto: ContextAware<PatientRegistrationDtoIn>, isinId: String?): EntityId {
        val registration = registrationDto.payload
        logger.debug { "Registering patient ${registration.email}." }

        logger.debug { "Registration validation." }
        validationService.requireValidRegistration(registration)

        logger.debug { "Saving registration." }
        return patientRepository.savePatient(
            firstName = registration.firstName.trim(),
            lastName = registration.lastName.trim(),
            zipCode = registration.zipCode,
            district = registration.district.trim(),
            phoneNumber = registration.phoneNumber.formatPhoneNumber(),
            personalNumber = registration.personalNumber?.normalizePersonalNumber(),
            insuranceNumber = registration.insuranceNumber?.trim(),
            email = registration.email.trim().lowercase(),
            insuranceCompany = registration.insuranceCompany,
            indication = registration.indication?.trim(),
            remoteHost = registrationDto.remoteHost,
            answers = registration.answers.associate { it.questionId to it.value },
            isinId = isinId
        ).also { patientId ->
            logger.debug { "Patient ${registration.email} saved under id $patientId." }
        }
    }

    /**
     * Deletes patient with given ID. Throws exception if patient was not deleted.
     * */
    suspend fun deletePatientById(patientId: UUID) {
        val deletedCount = patientRepository.deletePatientsBy { Patients.id eq patientId }
        if (deletedCount != 1) {
            throw entityNotFound<Patients>(Patients::id, patientId)
        }
    }

    private fun List<PatientDtoOut>.sorted() = map { it.withSortedAnswers() }.sortedBy { it.registeredOn }

    private fun PatientDtoOut.withSortedAnswers() = copy(answers = answers.sortedBy { it.questionId })

    private fun <T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this
}
