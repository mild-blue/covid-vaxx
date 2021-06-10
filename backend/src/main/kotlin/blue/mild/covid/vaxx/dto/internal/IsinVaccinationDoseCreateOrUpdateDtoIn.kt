package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.isin.Pracovnik
import java.time.Instant


// Fields according to api documentation https://apidoc.uzis.cz/index.html - VakcinaceDavkaCreateOrUpdate
data class IsinVaccinationDoseCreateOrUpdateDtoIn (
    val id: String?,
    val vakcinaceId: String,
    val ockovaciLatkaKod: String,
    val datumVakcinace: Instant,
    val typVykonuKod: String?,
    val sarze: String,
    val aplikacniCestaKod: String?,
    val mistoAplikaceKod: String?,
    val expirace: Instant,
    val poznamka: String?,
    val stav: String?, // Indikovano, Probihajici, Ukoncene, Zruseno
    val pracovnik: Pracovnik? = null
)
