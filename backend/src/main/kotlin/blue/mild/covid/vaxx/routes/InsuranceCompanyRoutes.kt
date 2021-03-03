package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.InsuranceCompany
import blue.mild.covid.vaxx.dto.InsuranceCompanyDetailsDtoOut
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.insuranceCompaniesRoutes() {

    route(Routes.insuranceCompanies) {
        get<Unit, List<InsuranceCompanyDetailsDtoOut>> {
            respond(InsuranceCompany.values().map(::InsuranceCompanyDetailsDtoOut))
        }
    }
}
