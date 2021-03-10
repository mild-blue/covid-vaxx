package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.Patient
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.PatientRegistrationDto
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegisteredDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.applyIf
import pw.forst.tools.katlib.whenFalse
import java.util.UUID

class PatientService(
    private val patientRepository: PatientRepository,
    private val validationService: ValidationService,
    private val entityIdProvider: EntityIdProvider
) {

    private companion object : KLogging()

    /**
     * Returns patient with given ID.
     */
    suspend fun getPatientById(patientId: UUID): PatientDtoOut =
        patientRepository.getAndMapPatientsBy { Patient.id eq patientId.toString() }
            .singleOrNull() ?: throw entityNotFound<Patient>(Patient::id, patientId)

    /**
     * Returns single patient with given personal number or throws exception.
     */
    suspend fun getPatientsByPersonalNumber(patientPersonalNumber: String): PatientDtoOut =
        patientRepository.getAndMapPatientsBy {
            Patient.personalNumber eq normalizePersonalNumber(patientPersonalNumber)
        }.singleOrNull() ?: throw entityNotFound<Patient>(Patient::personalNumber, patientPersonalNumber)

    /**
     * Filters the database with the conjunction (and clause) of the given properties.
     */
    suspend fun getPatientsByConjunctionOf(
        email: String? = null,
        phoneNumber: String? = null,
        vaccinated: Boolean? = null
    ): List<PatientDtoOut> =
        patientRepository.getAndMapPatientsBy {
            Op.TRUE
                .andWithIfNotEmpty(email?.trim(), Patient.email)
                .andWithIfNotEmpty(phoneNumber?.trim(), Patient.phoneNumber)
                .let { query ->
                    vaccinated?.let {
                        query.and(
                            if (vaccinated) Patient.vaccinatedOn.isNotNull()
                            else Patient.vaccinatedOn.isNull()
                        )
                    } ?: query
                }
        }

    /**
     * Returns all patients in the database.
     */
    suspend fun getAllPatients(): List<PatientDtoOut> =
        getPatientsByConjunctionOf()

    /**
     * Returns all patients with given email.
     */
    suspend fun getPatientsByEmail(email: String) =
        getPatientsByConjunctionOf(email = email)

    /**
     * Returns all patients with given email.
     */
    suspend fun getPatientsByPhoneNumber(phoneNumber: String) =
        getPatientsByConjunctionOf(phoneNumber = phoneNumber)


    /**
     * Returns all patients with given email.
     */
    suspend fun getPatientsByVaccinated(vaccinated: Boolean) =
        getPatientsByConjunctionOf(vaccinated = vaccinated)

    /**
     * Updates patient with given change set.
     */
    suspend fun updatePatientWithChangeSet(patientId: UUID, changeSet: PatientUpdateDtoIn) =
        patientRepository.updatePatientChangeSet(
            id = patientId,
            firstName = changeSet.firstName?.trim(),
            lastName = changeSet.lastName?.trim(),
            phoneNumber = changeSet.phoneNumber?.trim(),
            personalNumber = changeSet.personalNumber?.let { normalizePersonalNumber(it) },
            email = changeSet.email?.trim(),
            insuranceCompany = changeSet.insuranceCompany,
            vaccinatedOn = changeSet.vaccinatedOn,
            answers = changeSet.answers?.associate { it.questionId to it.value }
        ).whenFalse { throw entityNotFound<Patient>(Patient::id, patientId) }

    /**
     * Saves patient to the database.
     */
    suspend fun savePatient(patientRegistrationDto: PatientRegistrationDto): PatientRegisteredDtoOut {
        logger.debug { "Registering patient ${patientRegistrationDto.registration.email}" }

        val (registration, registrationRemoteHost) = patientRegistrationDto

        return PatientRegisteredDtoOut(newSuspendedTransaction {
            logger.debug { "Registration validation for patient ${registration.email}" }
            validationService.validatePatientRegistrationAndThrow(registration)

            val entityId = entityIdProvider.generateId()

            logger.debug { "Saving registration for patient ${registration.email}" }
            patientRepository.savePatient(
                id = entityId,
                firstName = registration.firstName.trim(),
                lastName = registration.lastName.trim(),
                phoneNumber = registration.phoneNumber.trim(),
                personalNumber = normalizePersonalNumber(registration.personalNumber),
                email = registration.email.trim(),
                insuranceCompany = registration.insuranceCompany,
                remoteHost = registrationRemoteHost,
                answers = registration.answers.associate { it.questionId to it.value }
            )
        }).also { (patientId) ->
            logger
                .applyIf(logger.isDebugEnabled) { debug { "Patient ${registration.email} saved under id $patientId" } }
                .applyIf(logger.isInfoEnabled) { info { "Patient $patientId registered." } }
        }
    }

    /**
     * Deletes patient with given ID. Throws exception if patient was not deleted.
     * */
    suspend fun deletePatientById(patientId: UUID) {
        val deletedCount = patientRepository.deletePatientsBy { Patient.id eq patientId.toString() }
        if (deletedCount != 1) {
            throw entityNotFound<Patient>(Patient::id, patientId)
        }
    }

    private fun normalizePersonalNumber(personalNumber: String): String =
        personalNumber.replace("/", "").trim()

    private fun <T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this
}
