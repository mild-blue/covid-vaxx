package blue.mild.covid.vaxx.dto.response

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class VaccinationSlotDtoOutTest {
    @Test
    fun `create rounded slot`() {
        val originalTo = Instant.parse("2021-06-01T10:28:30.00Z")
        val slot = VaccinationSlotDtoOut(
            id = UUID.randomUUID(),
            locationId = UUID.randomUUID(),
            patientId = UUID.randomUUID(),
            queue = 0,
            from = Instant.EPOCH,
            to = originalTo
        )

        val expectedTo = Instant.parse("2021-06-01T10:43:00.00Z")
        val expected = slot.copy(to = expectedTo)
        assertEquals(expected, slot.toRoundedSlot())
    }
}
