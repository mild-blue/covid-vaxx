package blue.mild.covid.vaxx.service

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationServiceTest {
    @Test
    fun `test mockk example`() {
        val invalidMail = "nothing"
        val mock = mockk<ValidationService>()
        every { mock.validateEmailAndThrow(invalidMail) } returns Unit

        assertEquals(mock.validateEmailAndThrow(invalidMail), Unit)
    }
}
