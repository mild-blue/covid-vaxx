package blue.mild.covid.vaxx.dto.config

import blue.mild.covid.vaxx.isin.Pracovnik

data class IsinConfigurationDto(
    val rootUrl: String,

    val pracovnik: Pracovnik,

    /**
     * Password for unlocking the store.
     */
    val storePass: String,

    /**
     * Certificate in base64 format
     */
    val certBase64: String,

    /**
     * Type of the store JKS for example.
     */
    val storeType: String,

    /**
     * Typically it is the same value as store password.
     *
     * Output while generating the certificate:
     * Warning:  Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified -keypass value.
     */
    val keyPass: String = storePass
)
