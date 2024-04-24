package model

import kotlinx.serialization.Serializable

@Serializable
data class HourlyUnits(
        val time: String,
        val temperature_2m: String,
        val relative_humidity_2m: String,
        val precipitation: String,
        val surface_pressure: String,
)

@Serializable
data class Hourly(
        val time: List<String>,
        val temperature_2m: List<Double>,
        val relative_humidity_2m: List<Int>,
        val precipitation: List<Double>,
        val surface_pressure: List<Double>,
)

@Serializable
data class WeatherResponse(
        val latitude: Double,
        val longitude: Double,
        val generationtime_ms: Double,
        val utc_offset_seconds: Int,
        val timezone: String,
        val timezone_abbreviation: String,
        val elevation: Double,
        val hourly_units: HourlyUnits,
        val hourly: Hourly
)

