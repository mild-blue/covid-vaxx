package blue.mild.covid.vaxx.setup

enum class EnvVariables {
    /**
     * Where the application version is stored.
     * Set during the Docker Image built.
     */
    RELEASE_FILE_PATH,

    /**
     * Connection properties for the database.
     */
    POSTGRES_DB,
    POSTGRES_HOST,
    POSTGRES_USER,
    POSTGRES_PASSWORD,

    /**
     * Isin configuration
     */
    ISIN_ROOT_URL,
    ISIN_PRACOVNIK_NRZP_CISLO,
    ISIN_PRACOVNIK_PCZ,
    ISIN_CERT_BASE64,
    ISIN_CERT_PASSWORD,
    ISIN_STORE_TYPE,

    // TODO certificate password decryption
    // KMS_KEY_ID,

    /**
     * If the MailJet email should be enabled.
     * If so, other envs need to specified as well.
     */
    ENABLE_MAIL_SERVICE,
    MAIL_JET_API_SECRET,
    MAIL_JET_API_KEY,
    MAIL_ADDRESS_FROM,
    MAIL_FROM,
    MAIL_SUBJECT,



    /**
     * From which path should backend serve static
     * frontend files. Set during Docker Image build.
     */
    FRONTEND_PATH,

    /**
     * What secret should be used for signing JWTs.
     * By default the application generates random string.
     *
     * This should be set to some value in production.
     */
    JWT_SIGNING_SECRET,

    /**
     * Time duration for how long should be JWTs valid in minutes.
     */
    JWT_EXPIRATION_IN_MINUTES,

    /**
     * Whether to enable rate limiting or not.
     * By default the rate limiting is enabled.
     */
    ENABLE_RATE_LIMITING,

    /**
     * Number of requests per [RATE_LIMIT_DURATION_MINUTES] that
     * should be allowed per host.
     */
    RATE_LIMIT,

    /**
     * Specifies period for [RATE_LIMIT] in minutes.
     */
    RATE_LIMIT_DURATION_MINUTES,

    /**
     * If swagger should be enabled or not. By default it is enabled,
     * but should be disabled in the production.
     */
    ENABLE_SWAGGER,

    /**
     * If the CORS should be allowed or not.
     * By default they're enabled for http://localhost:4200.
     *
     * Disable for production use.
     */
    ENABLE_CORS,

    /**
     * Allowed origins if [ENABLE_CORS] is enabled separated by ','.
     *
     * Example:
     * http://localhost:4200,https://covid-vaxx.stg.mild.blue
     */
    CORS_ALLOWED_HOSTS,

    /**
     * On which port should start server listening.
     * By default it is 8080.
     */
    PORT,

    /**
     * Enables production logging to JSON.
     *
     * Default false. Should be enabled on prod.
     */
    PRODUCTION_LOGGING,

    /**
     * Sets global log level - to all classes that are not
     * from the blue.mild package.
     *
     * Default INFO.
     */
    GLOBAL_LOG_LEVEL,

    /**
     * Log level for blue.mild package.
     *
     * Default TRACE.
     */
    LOG_LEVEL,

    /**
     * If the application should store logs in the file.
     *
     * Default false.
     */
    ENABLE_FILE_LOG,

    /**
     * Where to store the logs.
     *
     * Example: /var/logs/mildblue
     * Default: current runtime directory
     */
    FILE_FOLDER_LOG_PATH,

    /**
     * If the backend should require captcha verification during
     * saving the patients registration.
     */
    ENABLE_RECAPTCHA_VERIFICATION,

    /**
     * Secret key from Google Captcha.
     */
    RECAPTCHA_SECRET_KEY,

    /**
     * Use production ISIN to register vaccinations.
     */
    ENABLE_ISIN_REGISTRATION
}
