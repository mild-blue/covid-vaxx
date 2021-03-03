package blue.mild.covid.vaxx.routes

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import org.kodein.di.LazyDI

fun NormalOpenAPIRoute.registerRoutes(di: LazyDI) {
    patientRoutes(di)
    questionRoutes(di)
    insuranceCompanyRoutes()
    serviceRoutes(di)
}
