package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import com.fasterxml.jackson.annotation.JsonIgnore

data class InsuranceCompanyDetailsDtoOut(
    @JsonIgnore
    private val insuranceCompany: InsuranceCompany
) {
    val name = insuranceCompany.name
    val csFullName = insuranceCompany.csFullName
    val code = insuranceCompany.code
}
