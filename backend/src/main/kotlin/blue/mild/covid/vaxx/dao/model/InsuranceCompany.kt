package blue.mild.covid.vaxx.dao.model

enum class InsuranceCompany(
    val csFullName: String,
    val code: Int
) {
    VZP("Všeobecná zdravotní pojišťovna", 111),
    VOZP("Vojenská zdravotní pojišťovna", 201),
    CPZP("Česká průmyslová zdravotní pojišťovna", 205),
    OZP("Oborová zdravotní pojišťovna zaměstnanců bank, pojišťoven a stavebnictví", 207),
    ZPS("Zaměstnanecká pojišťovna Škoda", 209),
    ZPMV("Zdravotní pojišťovna ministerstva vnitra", 211),
    RBP("Revírní bratrská pokladna, zdravotní pojišťovna", 213)
}
