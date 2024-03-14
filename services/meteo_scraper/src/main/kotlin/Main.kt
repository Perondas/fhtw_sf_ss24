package at.fhtw
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import model.WeatherResponse

suspend fun main() {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val url = "https://api.open-meteo.com/v1/forecast?latitude=48.2085&longitude=16.3721&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,snow_depth,weather_code,surface_pressure,cloud_cover,wind_speed_10m,wind_direction_10m,soil_temperature_0cm&timezone=Europe%2FBerlin"
    val res = client.get(url)
    val weatherData = res.body<WeatherResponse>()
    println(weatherData)

    client.close()
}