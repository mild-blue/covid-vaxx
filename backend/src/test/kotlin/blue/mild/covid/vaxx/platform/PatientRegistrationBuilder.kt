package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.util.generatePersonalNumber
import blue.mild.covid.vaxx.utils.PatientRegistrationDtoInForTests
import java.util.UUID
import kotlin.random.Random

typealias PatientRegistrationBuilder = (answers: Any, insuranceCompany: Any) -> PatientRegistrationDtoInForTests


fun defaultPatientRegistrationBuilder(
    firstName: Any? = "[PerformanceTest] - ${UUID.randomUUID()}",
    lastName: Any? = "[PerformanceTest] - ${UUID.randomUUID()}",
    zipCode: Any? = 16000,
    district: Any? = "Prague 6",
    personalNumber: Any? = generatePersonalNumber(),
    phoneNumber: Any? = PhoneNumberDtoIn(number="601234567", countryCode = "CZ"),
    email: Any? = "${UUID.randomUUID()}@test.com",
    indication: Any? = "Teacher",
    confirmation: Any? = ConfirmationDtoIn(
        healthStateDisclosureConfirmation = true,
        covid19VaccinationAgreement = true,
        gdprAgreement = true
    )
): (Any, Any) -> PatientRegistrationDtoInForTests =
    { answers, insuranceCompany ->
        PatientRegistrationDtoInForTests(
            firstName = firstName,
            lastName = lastName,
            zipCode = zipCode,
            district = district,
            personalNumber = personalNumber,
            phoneNumber = phoneNumber,
            email = email,
            insuranceCompany = insuranceCompany,
            indication = indication,
            answers = answers,
            confirmation = confirmation,

        )
    }
