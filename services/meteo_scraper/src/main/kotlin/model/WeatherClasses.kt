package model;

import kotlinx.serialization.Serializable

@Serializable
data class HourlyUnits(
        val time: String,
        val temperature_2m: String,
        val relative_humidity_2m: String,
        val apparent_temperature: String,
        val precipitation_probability: String,
        val precipitation: String,
        val rain: String,
        val showers: String,
        val snowfall: String,
        val snow_depth: String,
        val weather_code: String,
        val surface_pressure: String,
        val cloud_cover: String,
        val wind_speed_10m: String,
        val wind_direction_10m: String,
        val soil_temperature_0cm: String
)

@Serializable
data class Hourly(
        val time: List<String>,
        val temperature_2m: List<Double>,
        val relative_humidity_2m: List<Int>,
        val apparent_temperature: List<Double>,
        val precipitation_probability: List<Int>,
        val precipitation: List<Double>,
        val rain: List<Double>,
        val showers: List<Double>,
        val snowfall: List<Double>,
        val snow_depth: List<Double>,
        val weather_code: List<String>,
        val surface_pressure: List<Double>,
        val cloud_cover: List<Int>,
        val wind_speed_10m: List<Double>,
        val wind_direction_10m: List<Int>,
        val soil_temperature_0cm: List<Double>
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

