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
            .singleOrNull()?.withSortedAnswers() ?: throw entityNotFound<Patient>(Patient::id, patientId)

    /**
     * Returns single patient with given personal number or throws exception.
     */
    suspend fun getPatientsByPersonalNumber(patientPersonalNumber: String): PatientDtoOut =
        patientRepository.getAndMapPatientsBy {
            Patient.personalNumber eq normalizePersonalNumber(patientPersonalNumber)
        }.singleOrNull()?.withSortedAnswers()
            ?: throw entityNotFound<Patient>(Patient::personalNumber, patientPersonalNumber)

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
                .andWithIfNotEmpty(email?.trim()?.toLowerCase(), Patient.email)
                .andWithIfNotEmpty(phoneNumber?.trim(), Patient.phoneNumber)
                .let { query ->
                    vaccinated?.let {
                        query.and(
                            if (vaccinated) Patient.vaccinatedOn.isNotNull()
                            else Patient.vaccinatedOn.isNull()
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
            phoneNumber = changeSet.phoneNumber?.trim(),
            personalNumber = changeSet.personalNumber?.let { normalizePersonalNumber(it) },
            email = changeSet.email?.trim()?.toLowerCase(),
            insuranceCompany = changeSet.insuranceCompany,
            vaccinatedOn = changeSet.vaccinatedOn,
            answers = changeSet.answers?.associate { it.questionId to it.value }
        ).whenFalse { throw entityNotFound<Patient>(Patient::id, patientId) }
    }

    /**
     * Saves patient to the database.
     */
    suspend fun savePatient(patientRegistrationDto: PatientRegistrationDto): PatientRegisteredDtoOut {
        logger.debug { "Registering patient ${patientRegistrationDto.registration.email}." }

        val (registration, registrationRemoteHost) = patientRegistrationDto

        logger.debug { "Registration validation." }
        validationService.requireValidRegistration(registration)

        return PatientRegisteredDtoOut(newSuspendedTransaction {
            val entityId = entityIdProvider.generateId()

            logger.debug { "Saving registration." }
            patientRepository.savePatient(
                id = entityId,
                firstName = registration.firstName.trim(),
                lastName = registration.lastName.trim(),
                phoneNumber = registration.phoneNumber.trim(),
                personalNumber = normalizePersonalNumber(registration.personalNumber),
                email = registration.email.trim().toLowerCase(),
                insuranceCompany = registration.insuranceCompany,
                remoteHost = registrationRemoteHost,
                answers = registration.answers.associate { it.questionId to it.value }
            )
        }).also { (patientId) ->
            logger.debug { "Patient ${registration.email} saved under id $patientId." }
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

    private fun List<PatientDtoOut>.sorted() = map { it.withSortedAnswers() }.sortedBy { it.created }

    private fun PatientDtoOut.withSortedAnswers() = copy(answers = answers.sortedBy { it.questionId })

    private fun normalizePersonalNumber(personalNumber: String): String =
        personalNumber.replace("/", "").trim()

    private fun <T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this
}
