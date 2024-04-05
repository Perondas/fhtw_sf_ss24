package at.fhtw

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import model.WeatherResponse
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

suspend fun main() {
    val weatherData = retrieveWeather()
    println(weatherData)

    val producer = configureProducer()

    producer.use {
        it.send(ProducerRecord("meteoWeather", "weather", weatherData.toString()))
    }
}

private suspend fun retrieveWeather(): WeatherResponse {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val baseUrl = "https://api.open-meteo.com/v1/forecast"
    val res = client.get {
        url(baseUrl)
        parameter("latitude", "48.2085")
        parameter("longitude", "16.3721")
        parameter(
            "hourly",
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,snow_depth,weather_code,surface_pressure,cloud_cover,wind_speed_10m,wind_direction_10m,soil_temperature_0cm"
        )
        parameter("timezone", "Europe/Berlin")
    }
    val weatherData = res.body<WeatherResponse>()

    client.close()
    return weatherData
}

private fun configureProducer(): KafkaProducer<String, String> {
    val producerProps = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9094",
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "security.protocol" to "PLAINTEXT"
    )

    return KafkaProducer(producerProps)
}
