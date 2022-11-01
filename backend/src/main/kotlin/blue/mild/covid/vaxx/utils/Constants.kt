package blue.mild.covid.vaxx.utils

import java.time.Instant

val defaultPostgresFrom: Instant = Instant.EPOCH

// this is maximal value for the Postgres
@Suppress("MagicNumber") // this is actually from the Postgres documentation
val defaultPostgresTo: Instant = Instant.ofEpochSecond(365241780471L)
