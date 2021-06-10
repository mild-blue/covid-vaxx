package blue.mild.covid.vaxx.isin

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook


fun main() {
    println("Using environment: ")
    val excelFile = FileInputStream(File("customers.xlsx"))
    val workbook = XSSFWorkbook(excelFile)

    val sheet = workbook.getSheet("Customers")
    val rows = sheet.iterator()
    while (rows.hasNext()) {
        val currentRow = rows.next()
        val cellsInRow = currentRow.iterator()
        while (cellsInRow.hasNext()) {
            val currentCell = cellsInRow.next()
            if (currentCell.getCellTypeEnum() === CellType.STRING) {
                print(currentCell.getStringCellValue() + " | ")
            } else if (currentCell.getCellTypeEnum() === CellType.NUMERIC) {
                print(currentCell.getNumericCellValue().toString() + "(numeric)")
            }
        }

        println()
    }

    workbook.close()
    excelFile.close()
}

