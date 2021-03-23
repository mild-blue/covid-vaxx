package blue.mild.covid.vaxx.dto.response

val OK = Ok()

data class Ok(
    val status: String = "ok"
)
