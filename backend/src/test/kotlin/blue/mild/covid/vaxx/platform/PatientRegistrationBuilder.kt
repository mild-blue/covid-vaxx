package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import java.util.UUID
import kotlin.random.Random

typealias PatientRegistrationBuilder = (answers: List<AnswerDto>, insuranceCompany: InsuranceCompany, confirmation: ConfirmationDtoIn) -> PatientRegistrationDtoIn

fun defaultPatientRegistrationBuilder(): PatientRegistrationBuilder =
    { answers, insuranceCompany, confirmation ->
        PatientRegistrationDtoIn(
            firstName = "[PerformanceTest] - ${UUID.randomUUID()}",
            lastName = "[PerformanceTest] - ${UUID.randomUUID()}",
            personalNumber = "7401040020",
            phoneNumber = "+420${(1..9).joinToString("") { Random.nextInt(10).toString() }}",
            email = "${UUID.randomUUID()}@test.com",
            insuranceCompany, answers, confirmation
        )
    }
