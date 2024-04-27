package at.fhtw

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import at.fhtw.model.HourlyForecastResponse
import com.fhtw.protobuf.WeatherData.Weather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Location(val zipCode: String, val name: String, val lat: Double, val lon: Double)

const val TopicName = "open-data"

suspend fun main() {
    val csv = object {}.javaClass.getResourceAsStream("/short-plz-coord-austria.csv")?.bufferedReader()?.readText()
        ?: throw IllegalStateException("Could not read CSV file")

    val locations = csv.split('\n').map { it.trim() }.filter { it.isNotBlank() }.map {
        val parts = it.split(';')
        Location(parts[0], parts[1], parts[3].toDouble(), parts[4].toDouble())
    }

    val props = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (System.getenv("KAFKA_SERVER") ?: "localhost:9094"),
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer",
        "security.protocol" to "PLAINTEXT",
        "schema.registry.url" to (System.getenv("SCHEMA_SERVER") ?: "localhost:8081"),
    )

    val client = HttpClient(CIO) {
        install(ContentNegotiation){
            json()
        }
    }

    val apiKey = "b1732995491144f25f24a6333dd3a5f6"
    val baseURL = "https://api.openweathermap.org/data/"
    val version = "2.5"

    val apiCurrentForecastURL = "$baseURL$version/forecast"

    val adminClient = AdminClient.create(props)
    if (!adminClient.listTopics().names().get().contains(TopicName)) {
        adminClient.createTopics(listOf(NewTopic(TopicName, 3, 2))).all().get()
    }
    adminClient.close()

    val producer = KafkaProducer<String, Weather>(props)

    timer(period = 1000 * 60 * 60 * 12, daemon = true) {
        CoroutineScope(Dispatchers.IO).launch {
            scrapeData(locations, client, apiCurrentForecastURL, apiKey, producer)
        }
    }

    awaitCancellation()
}

private suspend fun scrapeData(
    locations: List<Location>,
    client: HttpClient,
    apiCurrentForecastURL: String,
    apiKey: String,
    producer: Producer<String, Weather>
) {
    for (location in locations) {
        val res = client.get {
            url(apiCurrentForecastURL)
            parameter("lat", location.lat)
            parameter("lon", location.lon)
            parameter("appid", apiKey)
            parameter("units", "metric")
        }
        val data = res.body<HourlyForecastResponse>()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        for (weather in data.list) {
            val builder = Weather.newBuilder()

            val dateTime = LocalDateTime.parse(weather.dt_txt, formatter)
            val timestamp = dateTime.toEpochSecond(ZoneOffset.UTC)
            builder.setLatitude(location.lat)
            builder.setLongitude(location.lon)
            builder.setZipCode(location.zipCode)
            builder.setRegion(location.name)
            builder.setTimezone(data.city.timezone.toString())

            builder.setTime(timestamp)
            builder.setTemperature(weather.main.temp)
            builder.setRelativeHumidity(weather.main.humidity.toDouble()) //There is no other
            builder.setPrecipitation(weather.rain?.threeHours)
            builder.setSurfacePressure(weather.main.pressure.toDouble())

            val key = "open-${location.zipCode}-${timestamp}"

            producer.asyncSend(ProducerRecord(TopicName, key, builder.build()))
        }
    }
}

suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException) ?: continuation.resume(metadata)
        }
    }
