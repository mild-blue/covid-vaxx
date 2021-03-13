package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.error.EmptyStringException
import blue.mild.covid.vaxx.error.EmptyUpdateException
import blue.mild.covid.vaxx.error.PropertyValidationException
import mu.KLogging
import pw.forst.tools.katlib.mapToSet
import java.time.Instant
import java.time.LocalDate

@Suppress("TooManyFunctions") // this can't be split right now
class ValidationService(private val questionService: QuestionService) {
    private companion object : KLogging() {
        private const val requiredMinimalTimeOfVaccination = "2020-01-01T00:00:00.00Z"
        private const val personalNumberAddingTwentyIssueYear = 4
        private const val tenDigitPersonalNumberIssueYear = 54
        private const val womanMonthAddition = 50
        private const val unprobableMonthAddition = 20

        private val firstAllowedVaccinationTime by lazy {
            Instant.parse(requiredMinimalTimeOfVaccination)
        }
    }

    /**
     * Validates [patientRegistrationDto].
     *
     * Throws [PropertyValidationException] or [EmptyStringException] if any property
     * does not pass the validation process.
     */
    suspend fun requireValidRegistration(patientRegistrationDto: PatientRegistrationDtoIn) {
        // check empty strings first
        requireNotEmptyString("firstName", patientRegistrationDto.firstName)
        requireNotEmptyString("lastName", patientRegistrationDto.lastName)
        // now check specific cases
        requireValidPersonalNumber(patientRegistrationDto.personalNumber)
        requireValidPhoneNumber(patientRegistrationDto.phoneNumber)
        requireValidEmail(patientRegistrationDto.email)
        // check agreements
        requireTrue(
            "covid19VaccinationAgreement",
            patientRegistrationDto.confirmation.covid19VaccinationAgreement
        )
        requireTrue(
            "healthStateDisclosureConfirmation",
            patientRegistrationDto.confirmation.healthStateDisclosureConfirmation
        )

        requireTrue(
            "gdprAgreement",
            patientRegistrationDto.confirmation.gdprAgreement
        )

        // check answers to questions
        val answersByQuestion = patientRegistrationDto.answers.mapToSet { it.questionId }
        val allQuestions = questionService.getCachedQuestions().mapToSet { it.id }
        val diff = allQuestions.subtract(answersByQuestion)
        if (diff.isNotEmpty()) {
            throw PropertyValidationException(
                "answers",
                patientRegistrationDto.answers.joinToString(", ") {
                    "${it.questionId} -> ${it.value}"
                }
            )
        }
    }

    /**
     * Validates that the [changeSet] if it is valid.
     *
     * Throws [PropertyValidationException] or [EmptyStringException] if any property
     * does not pass the validation process.
     * Throws [EmptyUpdateException] if [changeSet] does not contain not null value.
     */
    fun requireValidPatientUpdate(changeSet: PatientUpdateDtoIn) {
        changeSet.firstName?.also { requireNotEmptyString("firstName", it) }
        changeSet.lastName?.also { requireNotEmptyString("lastName", it) }

        // now check specific cases
        changeSet.personalNumber?.also(::requireValidPersonalNumber)
        changeSet.phoneNumber?.also(::requireValidPhoneNumber)
        changeSet.email?.also(::requireValidEmail)
        changeSet.vaccinatedOn?.also(::requireValidVaccinatedOn)

        // now check that at least one property is changed, so we don't perform useless update
        changeSet.firstName ?: changeSet.lastName
        ?: changeSet.personalNumber ?: changeSet.email
        ?: changeSet.vaccinatedOn ?: throw EmptyUpdateException()
    }

    /**
     * Checks that the phone number is in the correct format with prefix +xyz and no spaces.
     *
     * Throws [PropertyValidationException] if the value is invalid.
     */
    fun requireValidPhoneNumber(phoneNumber: String) {
        if (!isPhoneNumberValid(phoneNumber.trim())) {
            throw PropertyValidationException("phoneNumber", phoneNumber)
        }
    }

    /**
     * Validates email - see https://emailregex.com.
     *
     * Throws [PropertyValidationException] if the value is invalid.
     */
    fun requireValidEmail(email: String) {
        if (!isEmailValid(email.trim())) {
            throw PropertyValidationException("email", email)
        }
    }

    /**
     * Validates correct format of [personalNumber].
     *
     * Throws [PropertyValidationException] if the value is [personalNumber] is not valid.
     */
    fun requireValidPersonalNumber(personalNumber: String) {
        val validationResult = runCatching {
            validatePersonalNumber(personalNumber.trim())
        }.getOrNull() ?: false
        if (!validationResult) {
            throw PropertyValidationException("personalNumber", personalNumber)
        }
    }

    /**
     * Checks if [value] is not empty.
     *
     * Throws [PropertyValidationException] if the value is empty.
     */
    fun requireNotEmptyString(parameterName: String, value: String) {
        if (value.isBlank()) {
            throw EmptyStringException(parameterName)
        }
    }

    /**
     * Checks if the [value] is true.
     *
     * Throws [PropertyValidationException] if the value is invalid.
     */
    fun requireTrue(parameterName: String, value: Boolean) {
        if (!value) {
            throw PropertyValidationException(parameterName, value)
        }
    }

    /**
     * Validates [vaccinatedOn], accepts only values after [requiredMinimalTimeOfVaccination].
     *
     * Throws [PropertyValidationException] if the value is invalid.
     */
    fun requireValidVaccinatedOn(vaccinatedOn: Instant) {
        if (vaccinatedOn.isBefore(firstAllowedVaccinationTime)) {
            logger.warn {
                "Vaccinated on $vaccinatedOn submitted. It is very unlikely, that this is correct as it is before " +
                        "$requiredMinimalTimeOfVaccination."
            }
            throw PropertyValidationException("vaccinatedOn", vaccinatedOn)
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean =
        """^\+\d{12}$""".toRegex() matches phoneNumber

    /**
     * Source: https://emailregex.com/
     */
    private fun isEmailValid(email: String): Boolean =
        """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|
            |"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\
            |x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a
            |-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]
            |))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]
            |:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e
            |-\x7f])+)])""".trimMargin()
            .toRegex() matches email

    // because those are correct indices
    // also because it is simply too complex to validate personal number
    @Suppress("MagicNumber", "ReturnCount", "ComplexMethod")
    private fun validatePersonalNumber(personalNumber: String): Boolean {
        if (personalNumber.length < 9) {
            return false
        }

        val firstPart: String
        val secondPart: String

        val parts = personalNumber.split("/")
        if (parts.size == 1) {
            firstPart = personalNumber.substring(0, 6)
            secondPart = personalNumber.substring(6)
        } else {
            firstPart = parts[0]
            secondPart = parts[1]
        }

        if (firstPart.length != 6 || !firstPart.isNumber() || !secondPart.isNumber()) {
            return false
        }

        val year = firstPart.substring(0, 2).toInt()
        var month = firstPart.substring(2, 4).toInt()
        val day = firstPart.substring(4, 6).toInt()

        val currentYear = LocalDate.now().year % 100

        if (year >= tenDigitPersonalNumberIssueYear || year <= currentYear) {
            if (secondPart.length == 4) {
                val controlDigit = secondPart.substring(3, 4).toInt()
                val concatenated = (firstPart + secondPart).toLong()

                val moduloElevenOk = concatenated % 11 == 0L
                val withoutLastDigit = concatenated / 10
                val moduloTenOk = (withoutLastDigit % 11) == 10L && controlDigit == 0

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
            month -= womanMonthAddition
        }

        if (month > unprobableMonthAddition) {
            if (year >= personalNumberAddingTwentyIssueYear) {
                month -= unprobableMonthAddition
            } else {
                return false
            }
        }

        return isDateValid(year, month, day)
    }

    private fun isDateValid(year: Int, month: Int, day: Int): Boolean =
        runCatching { LocalDate.of(year, month, day) }.isSuccess

    private fun String.isNumber(): Boolean = this.isNotEmpty() && this.toIntOrNull() != null
}
