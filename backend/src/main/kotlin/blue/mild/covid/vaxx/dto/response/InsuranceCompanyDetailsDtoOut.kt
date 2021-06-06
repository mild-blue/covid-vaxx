package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.InsuranceCompany

data class InsuranceCompanyDetailsDtoOut(
    val name: String,
    val csFullName: String,
    val code: InsuranceCompany,
    val numericCode: Int
) {
    constructor(company: InsuranceCompany) : this(
        name = company.name, csFullName = company.csFullName, code = company, numericCode = company.code
    )
}
