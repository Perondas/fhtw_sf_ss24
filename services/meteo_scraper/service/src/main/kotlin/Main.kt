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
import java.util.*
import kotlin.concurrent.timer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Location(val zipCode: String, val name: String, val lat: Double, val lon: Double)

const val TopicName = "meteo-data"

suspend fun main() {
    val csv = object {}.javaClass.getResourceAsStream("/short-plz-coord-austria.csv")?.bufferedReader()?.readText()
        ?: throw IllegalStateException("Could not read CSV file")

    val locations = csv.split('\n').map { it.trim() }.filter { it.isNotBlank() }.map {
        val parts = it.split(';')
        Location(parts[0], parts[1], parts[3].toDouble(), parts[4].toDouble())
    }

    val adminClient = configureAdminClient()
    if (!adminClient.listTopics().names().get().contains(TopicName)) {
        adminClient.createTopics(listOf(NewTopic(TopicName, 3, 2))).all().get()
    }
    adminClient.close()

    val producer = configureProducer()

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    // Every 12 hours
    timer(period = 1000 * 60 * 60 * 12, daemon = true) {
        CoroutineScope(Dispatchers.IO).launch {
            scrapeMeteo(locations, client, producer)
        }
    }

    awaitCancellation()
}

private suspend fun scrapeMeteo(locations: List<Location>, client: HttpClient, producer: Producer<String, Weather>) {
    for (location in locations) {
        val weatherResponse = retrieveWeather(client, location)
        println(weatherResponse)

        val weather: Weather =
            Weather.newBuilder()
                .setLatitude(weatherResponse.latitude)
                .setLongitude(weatherResponse.latitude)
                .setTimezone(weatherResponse.timezone)
                .setZipCode(location.zipCode)
                .setRegion(location.name)
                // TODO: Add the rest of the fields
                .build()

        // TODO: Add time of predictions in epoch seconds
        val key = "meteo-${location.zipCode}-${Date().time}"

        producer.asyncSend(ProducerRecord(TopicName, key, weather))
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
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,snow_depth,weather_code,surface_pressure,cloud_cover,wind_speed_10m,wind_direction_10m,soil_temperature_0cm"
        )
        parameter("timezone", "Europe/Berlin")
    }

    val weatherData = res.body<WeatherResponse>()

    return weatherData
}

private fun configureAdminClient(): AdminClient {
    val props = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (System.getenv("KAFKA_SERVER") ?: "localhost:9094"),
        "security.protocol" to "PLAINTEXT",
        "schema.registry.url" to (System.getenv("SCHEMA_SERVER") ?: "localhost:8081"),
    )

    return AdminClient.create(props)
}

private fun configureProducer(): Producer<String, Weather> {
    val props = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (System.getenv("KAFKA_SERVER") ?: "localhost:9094"),
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer",
        "security.protocol" to "PLAINTEXT",
        "schema.registry.url" to (System.getenv("SCHEMA_SERVER") ?: "localhost:8081"),
    )

    return KafkaProducer(props)
}

private suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException) ?: continuation.resume(metadata)
        }
    }

