package org.example

import org.example.protobuf.WeatherData.Weather
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.example.model.HourlyForecastResponse
import java.util.*

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation){
            json()
        }
    }

    val coordinateData = ExcelHandler().getCoordinateData()
    println(coordinateData)

    val apiKey = "b1732995491144f25f24a6333dd3a5f6"
    val baseURL = "https://api.openweathermap.org/data/"
    val version = "2.5"

    val apiCurrentForecastURL = "$baseURL$version/forecast"

    val res = client.get{
        url(apiCurrentForecastURL)
        parameter("lat", "48.21451155")
        parameter("lon", "16.52368505")
        parameter("appid", apiKey)
        parameter("units", "metric")
    }
    val data = res.body<HourlyForecastResponse>()

    println(data)

    client.close()

    val weather: Weather = Weather.newBuilder()
        .setLatitude(data.city.coord.lat)
        .setLongitude(data.city.coord.lon)
        .setTimezone(data.city.timezone.toString())
        .build()

    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9094"
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] =
        "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer"
    props["schema.registry.url"] = "http://127.0.0.1:8081"

    val producer = KafkaProducer<String, Weather>(props)

    val record: ProducerRecord<String, Weather> = ProducerRecord<String, Weather>("openWeather", "weather", weather)

    producer.send(record).get()
    producer.close()
}
