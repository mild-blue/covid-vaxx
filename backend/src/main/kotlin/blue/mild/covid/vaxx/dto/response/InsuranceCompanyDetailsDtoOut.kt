package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.InsuranceCompany

data class InsuranceCompanyDetailsDtoOut(
    val name: String,
    val csFullName: String,
    val code: InsuranceCompany
) {
    constructor(company: InsuranceCompany) : this(
        name = company.name, csFullName = company.csFullName, code = company
    )
}
