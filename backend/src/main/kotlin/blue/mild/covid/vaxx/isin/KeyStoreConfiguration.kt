package blue.mild.covid.vaxx.isin

/**
 * Configuration for the key store.
 */
data class KeyStoreConfiguration(

        /**
         * Password for unlocking the store.
         */
        val storePass: String,

        /**
         * Full path to the file.
         */
        val storePath: String,

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