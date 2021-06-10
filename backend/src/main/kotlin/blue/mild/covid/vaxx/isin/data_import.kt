package blue.mild.covid.vaxx.isin

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.request.AnswerDtoIn
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream


fun main() {
    println("Using environment: ")
    val excelFile =
        FileInputStream(File("D:\\odrive\\GDMildTK\\Shared with Me\\Leadership\\pacienti_poliklinka_6_11.xlsx"))
    val workbook = XSSFWorkbook(excelFile)

    val answers = listOf(
        AnswerDtoIn(
            EntityId.fromString("9a5587a1-dc43-49f3-9847-b736127c9e39"),
            false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("f4ca8d25-faaa-4b2f-abc2-3d7a8702d4a3"),
            value = false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("f5cf0689-a4d7-4c42-8107-6eaedca88a93"),
            value = false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("f68d221d-27a1-4c81-bf45-07b1f0290e15"),
            value = false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("f74ebe1e-ef94-4af0-963d-97ffab086b6b"),
            value = false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("f9c99047-0f44-4dfe-9964-71274a7af5e9"),
            value = false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("112f5fbd-cde2-4fe9-8cab-f5b4fff57296"),
            value = false
        ),
        AnswerDtoIn(
            questionId = EntityId.fromString("7b02b12a-abb4-45d3-8bf4-0b074e445f37"),
            value = false
        )
    )

    val confirmation = ConfirmationDtoIn(
        healthStateDisclosureConfirmation = true,
        covid19VaccinationAgreement = true,
        gdprAgreement = true
    )

    val sheet = workbook.getSheet("rezervace")
    val rows = sheet.iterator()
    while (rows.hasNext()) {
        val currentRow = rows.next()
        if (currentRow.getCell(0).toString() == "first_name") {
            continue
        }
        val insuranceCompany = when (currentRow.getCell(14).toString()) {
            "111" -> InsuranceCompany.VZP
            "201" -> InsuranceCompany.VOZP
            "205" -> InsuranceCompany.CPZP
            "207" -> InsuranceCompany.OZP
            "209" -> InsuranceCompany.ZPS
            "211" -> InsuranceCompany.ZPMV
            "213" -> InsuranceCompany.RBP
            else
            -> InsuranceCompany.VZP
        }
        val phoneNumner = PhoneNumberDtoIn(
            number = currentRow.getCell(2).toString().substring(4),
            countryCode = currentRow.getCell(2).toString().substring(0, 4)
        )
        val patientRegistrationDtoIn = PatientRegistrationDtoIn(
            firstName = currentRow.getCell(0).toString(),
            lastName = currentRow.getCell(1).toString(),
            answers = answers,
            confirmation = confirmation,
            district = currentRow.getCell(11).toString(),
            email = currentRow.getCell(3).toString(),
            insuranceCompany = insuranceCompany,
            insuranceNumber = null,
            personalNumber = currentRow.getCell(9).toString(),
            phoneNumber = phoneNumner,
            zipCode = currentRow.getCell(12).toString().replace(" ","").toInt(),

            )


        println(patientRegistrationDtoIn)
    }

    workbook.close()
    excelFile.close()
}

