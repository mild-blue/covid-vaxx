package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.utils.PatientRegistrationDtoInForTests
import java.util.UUID
import kotlin.random.Random

typealias PatientRegistrationBuilder = (answers: Any, insuranceCompany: Any) -> PatientRegistrationDtoInForTests


fun generateWithLeadingZero(from: Int, to: Int): String {
    val charCount = to.toString().length
    val num = (from..to).random().toString()
    val missingZeroes = charCount - num.length
    return "0".repeat(missingZeroes) + num
}

fun generatePersonalNumber(): String {

    val numberInDay = generateWithLeadingZero(0, 999)
    val year = generateWithLeadingZero(65, 99)
    val month = generateWithLeadingZero(1, 12)
    val day = generateWithLeadingZero(1, 28)

    val num = "$year$month$day$numberInDay"
    val rem = num.toBigInteger().rem("11".toBigInteger())
    if (rem != "10".toBigInteger()) {
        return "$num$rem"
    } else {
        return "${num}0"
    }

}


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
