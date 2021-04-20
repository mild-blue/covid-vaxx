package blue.mild.covid.vaxx.isin

/**
 * Configuration for /api/v1/vakcinace/VytvorNeboZmenVakcinaci
 */
data class VytvorNeboZmenVakcinaci(

        val pacientId: String,

        val typOckovaniKod: String,

        val indikace: List<String>,

        val pracovnik: Pracovnik
)


data class Pracovnik(

        val pcz: String,

        val pracovnikNrzpCislo: String

)


/**
 * Configuration for /api/v1/vakcinace/VytvorNeboZmenVakcinaci
 */
data class VytvorNeboZmenDavku(

        val vakcinaceId: String,

        val ockovaciLatkaKod: String,

        val sarze: String,

        val datumVakcinace: String,

        val expirace: String,

        val pracovnik: Pracovnik,

        val stav: String,

        val mistoAplikaceKod: String
)


/**
 * Configuration for /api/v1/vakcinace/VytvorNeboZmenVakcinaci
 */
data class AktualizujPacienta(

        val idPacienta: String,

        val kontaktniMobilniTelefon: String,

        val kontaktniEmail: String,

        val pracovnik: Pracovnik
)