package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.response.InsuranceCompanyDetailsDtoOut
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InsuranceCompanyRoutesTest : ServerTestBase(needsDatabase = false) {
    @Test
    fun `should return all insurance companies`() = withTestApplication {
        val expectedInsuranceCompanies = InsuranceCompany.values().map(::InsuranceCompanyDetailsDtoOut)
        handleRequest(HttpMethod.Get, Routes.insuranceCompanies).run {
            expectStatus(HttpStatusCode.OK)
            assertEquals(expectedInsuranceCompanies, receive())
        }
    }
}
