package at.fhtw

import com.fhtw.protobuf.WeatherData.Weather
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import model.WeatherResponse
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*

suspend fun main() {
    val weatherResponse = retrieveWeather()
    println(weatherResponse)

    // Use protobuf generated class and map
    val weather: Weather = Weather.newBuilder()
        .setLatitude(weatherResponse.latitude)
        .setLongitude(weatherResponse.latitude)
        .setTimezone(weatherResponse.timezone)
        .build()

    val producer = configureProducer()

    val record: ProducerRecord<String, Weather> = ProducerRecord<String, Weather>("meteoWeather", "weather", weather)
    producer.send(record).get()
    producer.close()

/*    producer.use {
        it.send(ProducerRecord("meteoWeather", "weather", weatherResponse.toString()))
    }*/
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

private fun configureProducer(): Producer<String, Weather> {
    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9094"
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] =
        "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer"
    props["schema.registry.url"] = "http://127.0.0.1:8081"

    return KafkaProducer(props)
}
