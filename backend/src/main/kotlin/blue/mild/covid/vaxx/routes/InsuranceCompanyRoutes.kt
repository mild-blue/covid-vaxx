package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.InsuranceCompany
import blue.mild.covid.vaxx.dto.response.InsuranceCompanyDetailsDtoOut
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

/**
 * Routes related to insurance company entity.
 */
fun NormalOpenAPIRoute.insuranceCompanyRoutes() {
    // TODO consider putting this behind auth
    // TODO #70 once ready, put behind captcha auth
    route(Routes.insuranceCompany) {
        get<Unit, List<InsuranceCompanyDetailsDtoOut>>(
            info("Returns list of all available insurance companies.")
        ) {
            respond(InsuranceCompany.values().map(::InsuranceCompanyDetailsDtoOut))
        }
    }
}
