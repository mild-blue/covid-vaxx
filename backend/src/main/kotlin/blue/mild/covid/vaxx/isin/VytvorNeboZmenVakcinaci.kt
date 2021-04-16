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
)