package at.fhtw

import com.fhtw.protobuf.WeatherData.Weather
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import model.WeatherResponse
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.concurrent.timer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Location(val zipCode: String, val name: String, val lat: Double, val lon: Double)

const val TopicName = "meteo-data"

val Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

suspend fun main() {
    val csv = object {}.javaClass.getResourceAsStream("/short-plz-coord-austria.csv")?.bufferedReader()?.readText()
        ?: throw IllegalStateException("Could not read CSV file")

    val locations = csv.split('\n').map { it.trim() }.filter { it.isNotBlank() }.map {
        val parts = it.split(';')
        Location(parts[0], parts[1], parts[3].toDouble(), parts[4].toDouble())
    }

    val props = createKafkaProperties()

    val adminClient = AdminClient.create(props)
    if (!adminClient.listTopics().names().get().contains(TopicName)) {
        adminClient.createTopics(listOf(NewTopic(TopicName, 3, 2))).all().get()
    }
    adminClient.close()

    val producer = KafkaProducer<String, Weather> (props)

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    // Every 12 hours
    timer(period = 1000 * 60 * 60 * 12, daemon = true) {
        CoroutineScope(Dispatchers.IO).launch {
            scrapeData(locations, client, producer)
        }
    }

    awaitCancellation()
}

private suspend fun scrapeData(locations: List<Location>, client: HttpClient, producer: Producer<String, Weather>) {
    val currentTime = LocalDateTime.now()

    for (location in locations.subList(0, 2)) { // TODO remove sublist - otherwise we might exceed api request limit
        val weatherResponse = retrieveWeather(client, location)
        println(weatherResponse)

        val b = Weather.newBuilder()

        b.setLatitude(location.lat)
        b.setLongitude(location.lon)
        b.setRegion(location.name)
        b.setZipCode(location.zipCode)
        b.setTimezone(weatherResponse.timezone)

        val hourly = weatherResponse.hourly
        for ((index, time) in hourly.time.withIndex()) {
            val timestamp = LocalDateTime.parse(time, Formatter)
            if (timestamp.isBefore(currentTime)) {
                continue
            }
            val epochSecond = timestamp.atOffset(ZoneOffset.UTC).toInstant().epochSecond

            b.setTime(epochSecond)
            b.setTemperature(hourly.temperature_2m[index])
            b.setRelativeHumidity(hourly.relative_humidity_2m[index].toDouble())
            b.setPrecipitation(hourly.precipitation[index])
            b.setSurfacePressure(hourly.surface_pressure[index])

            val data = b.build()
            val key = "meteo-${location.zipCode}-${epochSecond}"

            println(data)
            producer.asyncSend(ProducerRecord(TopicName, key, data))
        }
    }
}

private suspend fun retrieveWeather(client: HttpClient, location: Location): WeatherResponse {
    val baseUrl = "https://api.open-meteo.com/v1/forecast"
    val res = client.get {
        url(baseUrl)
        parameter("latitude", location.lat)
        parameter("longitude", location.lon)
        parameter(
            "hourly",
            "temperature_2m,relative_humidity_2m,precipitation,surface_pressure"
        )
        parameter("timezone", "Europe/Berlin")
        parameter("forecast_days", "3")
    }

    return res.body<WeatherResponse>()
}

private fun createKafkaProperties(): Map<String, String> {
    return mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (System.getenv("KAFKA_SERVER") ?: "localhost:9094"),
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer",
        "security.protocol" to "PLAINTEXT",
        "schema.registry.url" to (System.getenv("SCHEMA_SERVER") ?: "http://localhost:8081"),
    )
}

private suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException) ?: continuation.resume(metadata)
        }
    }

