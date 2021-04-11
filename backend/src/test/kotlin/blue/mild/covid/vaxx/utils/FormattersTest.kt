package blue.mild.covid.vaxx.utils

import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class FormattersTest {
    @Test
    fun `should format phone number`() {
        var result = PhoneNumberDtoIn(number = " -  420 7 36 11 456 7", countryCode = "CZ").formatPhoneNumber()
        assertEquals("+420736114567", result)

        result = PhoneNumberDtoIn(number = " -  7 36 11 456 7", countryCode = "CZ").formatPhoneNumber()
        assertEquals("+420736114567", result)
    }

    @Test
    fun `should remove all whitespaces from string`() {
        val result = "# @345l      khj      345+fdgd  fg$ # %35  ".removeAllWhitespaces()
        assertEquals("#@345lkhj345+fdgdfg$#%35", result)
    }
}
