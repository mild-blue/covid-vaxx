package blue.mild.covid.vaxx.service

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantTimeProvider {
    fun now(): Instant = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
}
