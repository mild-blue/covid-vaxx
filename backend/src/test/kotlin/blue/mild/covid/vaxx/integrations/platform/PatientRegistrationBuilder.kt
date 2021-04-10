package blue.mild.covid.vaxx.integrations.platform

import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.integrations.PatientRegistrationDtoInForTests
import blue.mild.covid.vaxx.utils.generatePersonalNumber
import java.util.UUID
import kotlin.random.Random

typealias PatientRegistrationBuilder = (answers: Any, insuranceCompany: Any) -> PatientRegistrationDtoInForTests


fun defaultPatientRegistrationBuilder(
    firstName: Any? = "[PerformanceTest] - ${UUID.randomUUID()}",
    lastName: Any? = "[PerformanceTest] - ${UUID.randomUUID()}",
    personalNumber: Any? = generatePersonalNumber(),
    phoneNumber: Any? = "+420${(1..9).joinToString("") { Random.nextInt(10).toString() }}",
    email: Any? = "${UUID.randomUUID()}@test.com",
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
            personalNumber = personalNumber,
            phoneNumber = phoneNumber,
            email = email,
            insuranceCompany, answers, confirmation
        )
    }
