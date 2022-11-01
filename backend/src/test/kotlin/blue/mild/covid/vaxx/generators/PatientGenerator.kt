package blue.mild.covid.vaxx.generators

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.model.Questions
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.AnswerDtoIn
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.utils.formatPhoneNumber
import blue.mild.covid.vaxx.utils.generatePersonalNumber
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.Locale
import java.util.UUID

suspend fun PatientRepository.generatePatientInDatabase(): PatientDtoOut {
    val patient = generatePatientRegistrationDto()

    val patientId = savePatient(
        firstName = patient.firstName.trim(),
        lastName = patient.lastName.trim(),
        zipCode = patient.zipCode,
        district = patient.district.trim(),
        phoneNumber = patient.phoneNumber.formatPhoneNumber(),
        personalNumber = patient.personalNumber?.normalizePersonalNumber(),
        insuranceNumber = patient.insuranceNumber?.trim(),
        email = patient.email.trim().lowercase(Locale.getDefault()),
        insuranceCompany = patient.insuranceCompany,
        indication = patient.indication?.trim(),
        remoteHost = "0.0.0.0",
        answers = patient.answers.associate { it.questionId to it.value },
        isinId = null
    )

    return requireNotNull(getAndMapById(patientId)) { "The patient was not created!" }
}

suspend fun generatePatientRegistrationDto() = newSuspendedTransaction {
    Questions.selectAll().map { AnswerDtoIn(it[Questions.id], true) }
}.let { generatePatientRegistrationDto(it) }

fun generatePatientRegistrationDto(answers: List<AnswerDtoIn>) = PatientRegistrationDtoIn(
    firstName = UUID.randomUUID().toString(),
    lastName = UUID.randomUUID().toString(),
    zipCode = 1600,
    district = "Praha 6",
    personalNumber = generatePersonalNumber(),
    insuranceNumber = null,
    phoneNumber = PhoneNumberDtoIn("721680111", "CZ"),
    email = "${UUID.randomUUID()}@mild.blue",
    insuranceCompany = InsuranceCompany.values().random(),
    indication = null,
    answers = answers,
    confirmation = ConfirmationDtoIn(
        healthStateDisclosureConfirmation = true,
        covid19VaccinationAgreement = true,
        gdprAgreement = true
    )
)
