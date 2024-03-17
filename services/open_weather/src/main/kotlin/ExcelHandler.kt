package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.example.model.CoordinateModel
import java.io.FileInputStream
import java.nio.file.Paths
import java.sql.Date

class ExcelHandler {

    suspend fun getCoordinateData(): List<CoordinateModel> {
//        val inputStream = this::class.java.getResourceAsStream("plz-coord-austria-short_sheet2.xlsx")
        val path = Paths.get("").toAbsolutePath().toString().replace("/services", "")
        val fullPath = "$path/data/AT_plz_coordinates/plz-coord-austria-short_sheet2.xlsx"
        println("path: $fullPath")
        val inputStream = withContext(Dispatchers.IO) {
            FileInputStream(fullPath)
        }
        val workbook = WorkbookFactory.create(inputStream)

        val workSheet = workbook.getSheetAt(3);

        val coordinatesModel: MutableList<CoordinateModel> = mutableListOf()
        val lastNumber = workSheet.lastRowNum

        for (i in 2..workSheet.physicalNumberOfRows) {
//            if ( row.rowNum <= 3 ) { continue }
            if(i >= workSheet.lastRowNum-1) { continue }

            val plz = workSheet.getRow(i).getCell(0).numericCellValue.toInt()
            val destination = workSheet.getRow(i).getCell(1).stringCellValue
            val creationDate = workSheet.getRow(i).getCell(2).stringCellValue
            val lat = workSheet.getRow(i).getCell(3).stringCellValue.toDouble()
            val lon = workSheet.getRow(i).getCell(4).stringCellValue.toDouble()
//            println("plz: $plz, destination: $destination, lat: $lat, lon: $lon")

            val coordinate = CoordinateModel(
                    plz = plz,
                    destination = destination,
                    creationDate = creationDate,
                    lat = lat,
                    lon = lon
                )

            coordinatesModel += coordinate
        }

        return coordinatesModel.toList()
    }
}