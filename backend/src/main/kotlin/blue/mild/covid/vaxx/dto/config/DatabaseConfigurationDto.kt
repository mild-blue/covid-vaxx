package blue.mild.covid.vaxx.dto.config

/**
 * Simple configuration for the database connection.
 */
data class DatabaseConfigurationDto(
    /**
     * Username for login.
     */
    val userName: String,
    /**
     * Password for login.
     */
    val password: String,
    /**
     * URL where the database is running.
     */
    val url: String
)
