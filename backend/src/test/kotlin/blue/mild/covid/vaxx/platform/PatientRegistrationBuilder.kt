package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dao.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn

typealias PatientRegistrationBuilder = (answers: List<AnswerDto>, insuranceCompany: InsuranceCompany, confirmation: ConfirmationDtoIn) -> PatientRegistrationDtoIn

fun defaultPatientRegistrationBuilder(): PatientRegistrationBuilder =
    { answers, insuranceCompany, confirmation ->
        PatientRegistrationDtoIn(
            firstName = "[PerformanceTest] - John",
            lastName = "[PerformanceTest] - Doe",
            personalNumber = "7401040020",
            phoneNumber = "+420123456789",
            email = "performance@test.com",
            insuranceCompany, answers, confirmation
        )
    }
