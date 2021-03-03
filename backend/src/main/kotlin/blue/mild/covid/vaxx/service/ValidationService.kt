package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.error.EmptyStringException
import blue.mild.covid.vaxx.error.ValidationException
import kotlinx.coroutines.runBlocking
import pw.forst.tools.katlib.mapToSet
import java.time.LocalDate

class ValidationService(private val questionService: QuestionService) {
    companion object {
        private const val personalNumberAddingTwentyIssueYear = 4;
        private const val tenDigitPersonalNumberIssueYear = 54;
        private const val womanMonthAddition = 50;
        private const val unprobableMonthAddition = 20;
    }

    fun validatePatientRegistrationAndThrow(patientRegistrationDto: PatientRegistrationDtoIn) {
        validateEmptyStringAndThrow("firstName", patientRegistrationDto.firstName)
        validateEmptyStringAndThrow("lastName", patientRegistrationDto.lastName)
        validatePersonalNumberAndThrow(patientRegistrationDto.personalNumber)
        validatePhoneNumberAndThrow(patientRegistrationDto.phoneNumber)
        validateEmailAndThrow(patientRegistrationDto.email)
        validateTrueAndThrow(
            "covid19VaccinationAgreement",
            patientRegistrationDto.confirmation.covid19VaccinationAgreement
        )
        validateTrueAndThrow(
            "healthStateDisclosureConfirmation",
            patientRegistrationDto.confirmation.healthStateDisclosureConfirmation
        )

        val answersByQuestion = patientRegistrationDto.answers.mapToSet { it.questionId }
        val allQuestions = runBlocking { questionService.getAllQuestions() }.mapToSet { it.id }
        val diff = allQuestions.subtract(answersByQuestion)
        if (diff.isNotEmpty()) {
            throw ValidationException(
                "answers",
                patientRegistrationDto.answers.joinToString(",") { "${it.questionId} -> ${it.value}" }
            )
        }
    }

    fun validatePhoneNumberAndThrow(phoneNumber: String) {
        if (!validatePhoneNumber(phoneNumber)) {
            throw ValidationException("phoneNumber", phoneNumber)
        }
    }

    fun validateEmailAndThrow(email: String) {
        if (!validateEmail(email)) {
            throw ValidationException("email", email)
        }
    }

    fun validatePersonalNumberAndThrow(personalNumber: String) {
        if (!validatePersonalNumber(personalNumber)) {
            throw ValidationException("personalNumber", personalNumber)
        }
    }

    fun validateEmptyStringAndThrow(parameterName: String, value: String) {
        if (!validateEmptyString(value)) {
            throw EmptyStringException(parameterName)
        }
    }

    fun validateTrueAndThrow(parameterName: String, value: Boolean) {
        if (!value) {
            throw ValidationException(parameterName, value)
        }
    }

    private fun validateEmptyString(value: String): Boolean = value.isNullOrEmpty()

    private fun validatePhoneNumber(phoneNumber: String): Boolean = """^\+\d{12}$""".toRegex() matches phoneNumber

    /**
     * Source: https://emailregex.com/
     *
     * @param email
     * @return
     */
    private fun validateEmail(email: String): Boolean =
        """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".toRegex() matches email

    private fun validatePersonalNumber(personalNumber: String): Boolean {
        if (personalNumber.isNullOrEmpty()) {
            return false
        }

        var firstPart = ""
        var secondPart = ""

        val parts = personalNumber.split("/");
        if (parts.size == 1) {
            firstPart = personalNumber.substring(0, 5);
            secondPart = personalNumber.substring(6);
        } else {
            firstPart = parts[0];
            secondPart = parts[1];
        }

        if (firstPart.length != 6 || !firstPart.isNumber() || !secondPart.isNumber()) {
            return false
        }

        val year = firstPart.substring(0, 1).toInt()
        var month = firstPart.substring(2, 3).toInt()
        val day = firstPart.substring(4, 5).toInt()

        val currentYear = LocalDate.now().year % 100

        if (year >= tenDigitPersonalNumberIssueYear || year <= currentYear) {
            if (secondPart.length == 4) {
                val controlDigit = secondPart.substring(3, 3).toInt()
                val concatenated = (firstPart + secondPart).toInt()

                val moduloElevenOk = concatenated % 11 == 0;
                val withoutLastDigit = concatenated / 10;
                val moduloTenOk = (withoutLastDigit % 11) == 10 && controlDigit == 0;

                if (!moduloTenOk && !moduloElevenOk) {
                    return false
                }
            } else {
                return false
            }
        } else {
            if (secondPart.length != 3) {
                return false
            }
        }
        if (month > womanMonthAddition) {
            month -= womanMonthAddition;
        }

        if (month > unprobableMonthAddition) {
            if (year >= personalNumberAddingTwentyIssueYear) {
                month -= unprobableMonthAddition;
            } else {
                return false
            }
        }

        return isDateValid(year, month, day)
    }


    private fun isDateValid(year: Int, month: Int, day: Int): Boolean {
        try {
            LocalDate.of(year, month, day)
            return true
        } catch (ex: Exception) {
            return false
        }
    }
}

fun String.isNumber(): Boolean = this.isNotEmpty() && this.toIntOrNull() != null
