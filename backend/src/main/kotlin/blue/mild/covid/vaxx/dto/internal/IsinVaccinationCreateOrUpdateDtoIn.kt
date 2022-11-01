package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.isin.Pracovnik

// Fields according to api documentation https://apidoc.uzis.cz/index.html - VakcinaceCreateOrUpdate
data class IsinVaccinationCreateOrUpdateDtoIn(
    val id: String?,
    val pacientId: String,
    val typOckovaniKod: String,
    val indikace: List<String>,
    val indikaceJina: String?,
    val pracovnik: Pracovnik? = null
)
