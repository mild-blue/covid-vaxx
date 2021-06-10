package blue.mild.covid.vaxx.dto.internal


// Fields according to api documentation https://apidoc.uzis.cz/index.html - Uzivatel
data class IsinSubjectDto(
    val subjektIco: Int,
    val nazevPracoviste: String?,
    val jmenoAPrijmeniUzivatele: String?
)
