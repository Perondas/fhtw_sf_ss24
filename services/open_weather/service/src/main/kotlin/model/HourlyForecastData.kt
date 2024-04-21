package at.fhtw.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int?,
    val sea_level : Int,
    val grnd_level : Int,
    val humidity : Int,
    val temp_kf : Double,
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String //Note this is realy a string not an url
)

@Serializable
data class Clouds(
    val all: Int
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

@Serializable
data class Rain(
    @SerialName("3h")
    val threeHours: Double?
)

@Serializable
data class System(
    val pod: String
)

@Serializable
data class WeatherData(
    val dt: Int,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain? = null,
    val sys: System,
    val dt_txt: String
)

@Serializable
data class Coordinates (
    val lat: Double,
    val lon: Double
)

@Serializable
data class City (
    val id: Int,
    val name: String,
    val coord: Coordinates,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Int,
    val sunset: Int
)

@Serializable
data class HourlyForecastResponse (
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<WeatherData>,
    val city: City
)