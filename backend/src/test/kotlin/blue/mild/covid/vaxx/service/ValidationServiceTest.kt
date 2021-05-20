package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.request.AnswerDtoIn
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.error.EmptyStringException
import blue.mild.covid.vaxx.error.EmptyUpdateException
import blue.mild.covid.vaxx.error.PropertyValidationException
import blue.mild.covid.vaxx.utils.generatePersonalNumber
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID
import java.util.stream.Stream
import kotlin.random.Random


class ValidationServiceTest {
    private companion object {
        @JvmStatic
        @Suppress("unused") // it is used during runtime
        fun validPersonalNumbers(): Stream<Arguments> = Stream.of(
            Arguments.of(generatePersonalNumber()), // random personal number
            Arguments.of("330223/183"), // old man
            Arguments.of("296127/870"), // old woman
            Arguments.of("280307974"), // old man without /
            Arguments.of("255816339"), // old woman without /
            Arguments.of("870528/2861"), // man
            Arguments.of("085922/5796"), // woman
            Arguments.of("0757236216"), // woman without /
            Arguments.of("0412101712"), // man without /
        )
    }

    @Test
    fun `test empty district`() {
        val instance = instance()
        val registration = validRegistration()
        assertDoesNotThrow {
            runBlocking { instance.requireValidRegistration(registration) }
        }
        assertThrows<EmptyStringException> {
            runBlocking { instance.requireValidRegistration(registration.copy(district = "")) }
        }
    }

    @Test
    fun `test validate zip code`() {
        val instance = instance()
        assertDoesNotThrow { instance.requireValidZipCode(16000) }
        assertThrows<PropertyValidationException> { instance.requireValidZipCode(0) }
    }

    @Test
    fun `test validate change set`() {
        val instance = instance()
        val validChangeSet = PatientUpdateDtoIn(firstName = "John")
        assertDoesNotThrow { instance.requireValidPatientUpdate(validChangeSet) }
        val invalidChangeSet = PatientUpdateDtoIn()
        assertThrows<EmptyUpdateException> {
            instance.requireValidPatientUpdate(invalidChangeSet)
        }
    }

    @Test
    fun `test validate registration - confirmation is false`() {
        val instance = instance()
        val registration = validRegistration()
            .copy(
                confirmation = ConfirmationDtoIn(
                    healthStateDisclosureConfirmation = false,
                    covid19VaccinationAgreement = true,
                    gdprAgreement = true
                )
            )

        assertThrows<PropertyValidationException> {
            runBlocking { instance.requireValidRegistration(registration) }
        }
    }

    @Test
    fun `test validate registration - missing questions`() {
        val questions = listOf(
            QuestionDtoOut(
                id = UUID.randomUUID(),
                placeholder = "hello",
                cs = "world",
                eng = "!"
            ),
            QuestionDtoOut(
                id = UUID.randomUUID(),
                placeholder = "hello2",
                cs = "world2",
                eng = "!2"
            )
        )
        val instance = instance(questionService(questions))
        val registration = validRegistration(questions)
            .copy(answers = listOf(AnswerDtoIn(questions[0].id, true)))

        assertThrows<PropertyValidationException> {
            runBlocking { instance.requireValidRegistration(registration) }
        }
    }

    @Test
    fun `test validate whole registration`() {
        val questions = listOf(
            QuestionDtoOut(
                id = UUID.randomUUID(),
                placeholder = "hello",
                cs = "world",
                eng = "!"
            )
        )
        val instance = instance(questionService(questions))
        val registration = validRegistration(questions)

        assertDoesNotThrow {
            runBlocking { instance.requireValidRegistration(registration) }
        }
    }

    @ParameterizedTest(name = "Invalid personal number: \"{0}\"")
    @ValueSource(
        strings = [
            "330223/183.", // dot at the end of the string
            "280a307974", // letter inside the number
            "085922-5796", // - instead of /
            "04/12101712", // / on wrong place
            "0757236217", // can not be divided by 11
        ]
    )
    fun `test validate personal number - invalid numbers`(personalNumber: String) {
        assertThrows<PropertyValidationException> {
            instance().requireValidPersonalNumber(personalNumber)
        }
    }

    @ParameterizedTest(name = "Valid personal number: \"{0}\"")
    @MethodSource("validPersonalNumbers")
    fun `test valid personal number`(validPersonalNumber: String) {
        assertDoesNotThrow {
            instance().requireValidPersonalNumber(validPersonalNumber)
        }
    }

    @Test
    fun `test empty string`() {
        val instance = instance()
        assertThrows<EmptyStringException> {
            instance.requireNotEmptyString("", "")
        }
        assertThrows<EmptyStringException> {
            instance.requireNotEmptyString("", "     ")
        }
        assertThrows<EmptyStringException> {
            instance.requireNotEmptyString("", "\t")
        }
        assertDoesNotThrow {
            instance.requireNotEmptyString("", "a")
        }
    }

    @Test
    fun `test validate true`() {
        val instance = instance()
        assertThrows<PropertyValidationException> {
            instance.requireTrue("", false)
        }
        assertDoesNotThrow {
            instance.requireTrue("", true)
        }
    }

    @ParameterizedTest(name = "Invalid character in email: \"{0}\"")
    @ValueSource(
        strings = [
            "hel\"o@mild.blue",
            "hel<o@mild.blue",
            "hel>o@mild.blue",
            "hel;o@mild.blue",
            "hel o@mild.blue",
        ]
    )
    fun `test validate email - invalid characters`(email: String) {
        assertThrows<PropertyValidationException> {
            instance().requireValidEmail(email)
        }
    }

    @Test
    fun `test validate email - missing name`() {
        val email = "@mild.blue"
        assertThrows<PropertyValidationException> {
            instance().requireValidEmail(email)
        }
    }

    @Test
    fun `test validate email - ending on dot`() {
        val email = "${UUID.randomUUID()}@mild."
        assertThrows<PropertyValidationException> {
            instance().requireValidEmail(email)
        }
    }

    @Test
    fun `test validate email - missing domain`() {
        val email = "${UUID.randomUUID()}@"
        assertThrows<PropertyValidationException> {
            instance().requireValidEmail(email)
        }
    }

    @Test
    fun `test validate email - missing @`() {
        val email = "${UUID.randomUUID()}mild.blue"
        assertThrows<PropertyValidationException> {
            instance().requireValidEmail(email)
        }
    }

    @ParameterizedTest(name = "Valid email: \"{0}\"")
    @ValueSource(
        strings = [
            "hello123@mild.blue",
            "hello+world@mild.blue",
            "hello@mild.blue",
        ]
    )
    fun `test valid email`(validEmail: String) {
        assertDoesNotThrow {
            instance().requireValidEmail(validEmail)
        }
    }

    @Test
    fun `test validate invalid phone number - wrong length`() {
        val phoneNumber = generateValidCzPhoneNumber()
        val phoneWithMoreNumbers = "${phoneNumber.number}1"
        assertThrows<PropertyValidationException> {
            instance().requireValidPhoneNumber(phoneWithMoreNumbers, phoneNumber.countryCode)
        }
    }

    @Test
    fun `test validate invalid phone number - contains letter`() {
        // as there's zero at least in prefix, this will fail
        val phoneNumber = generateValidCzPhoneNumber()
        val phoneWithLetter = phoneNumber.number.replace('0', 'a')
        assertThrows<PropertyValidationException> {
            instance().requireValidPhoneNumber(phoneWithLetter, phoneNumber.countryCode)
        }
    }

    @Test
    fun `test validate correct phone number`() {
        assertDoesNotThrow {
            val phoneNumber = generateValidCzPhoneNumber()
            instance().requireValidPhoneNumber(phoneNumber.number, phoneNumber.countryCode)
        }
    }

    private fun validRegistration(questions: List<QuestionDtoOut> = emptyList()) =
        PatientRegistrationDtoIn(
            firstName = "John",
            lastName = "Doe",
            zipCode = 16000,
            district = "Praha 6",
            personalNumber = generatePersonalNumber(),
            phoneNumber = generateValidCzPhoneNumber(),
            email = "john@mild.blue",
            insuranceCompany = InsuranceCompany.ZPMV,
            answers = questions.map { AnswerDtoIn(it.id, true) },
            confirmation = ConfirmationDtoIn(
                healthStateDisclosureConfirmation = true,
                covid19VaccinationAgreement = true,
                gdprAgreement = true
            )
        )

    private fun questionService(
        questions: List<QuestionDtoOut> = emptyList()
    ) = mockk<QuestionService> {
        coEvery { getCachedQuestions() } returns questions
    }

    private fun instance(questionService: QuestionService = questionService()) =
        ValidationService(questionService)

    private fun generateValidCzPhoneNumber() = PhoneNumberDtoIn(
        "+420736 ${(1..6).map { Random.nextInt(0, 10) }.joinToString("")}",
        "CZ"
    )

}
