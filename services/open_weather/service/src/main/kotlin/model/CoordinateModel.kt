package at.fhtw.model

import java.util.Date

data class CoordinateModel (
    val plz: Int?,
    val destination: String?,
    val creationDate: String?,
    val lat: Double,
    val lon: Double
)