package at.fhtw

import at.fhwt.model.TimeseriesForecastGeoJSONSerializer
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
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.time.format.TextStyle
import java.util.*
import kotlin.concurrent.timer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Location(val zipCode: String, val name: String, val lat: Double, val lon: Double)

const val TopicName = "geosphere-data"

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

    val adminClient = AdminClient.create(props);

    adminClient.createTopics(listOf(NewTopic(TopicName, 3, 2))).all().get()

    val producer = KafkaProducer<String, Weather>(props)

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
    // We chunk the locations into groups of 30 to avoid hitting the API rate limit
    for (locationC in locations.chunked(30)) {
        val res = client.get("https://dataset.api.hub.geosphere.at/v1/timeseries/forecast/nwp-v1-1h-2500m") {
            parameter("parameters", "t2m")
            parameter("parameters", "rh2m")
            parameter("parameters", "rr_acc")
            parameter("parameters", "sp")

            for (location in locationC) {
                parameter("lat_lon", "${location.lat},${location.lon}")
            }
        }

        val forecasts = res.body<TimeseriesForecastGeoJSONSerializer>()

        for ((lIndex, location) in locationC.withIndex()) {
            for ((tIndex, timestamp) in forecasts.timestamps.withIndex()) {
                val key = "geosphere-${location.zipCode}-${timestamp.toInstant().epochSecond}"

                val params = forecasts.features[lIndex].properties.parameters

                val b = Weather.newBuilder()

                b.setLatitude(location.lat)
                b.setLongitude(location.lon)
                b.setTimezone(forecasts.referenceTime.zone.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                b.setZipCode(location.zipCode)
                b.setRegion(location.name)
                b.setTime(timestamp.toInstant().epochSecond)

                b.setTemperature(params["t2m"]!!.data[tIndex]!!)
                b.setRelativeHumidity(params["rh2m"]!!.data[tIndex]!!)
                b.setPrecipitation(params["rr_acc"]!!.data[tIndex]!!)
                b.setSurfacePressure(params["sp"]!!.data[tIndex]!!)

                val data = b.build()

                producer.asyncSend(ProducerRecord(TopicName, key, data))
            }
        }
    }
}

suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException) ?: continuation.resume(metadata)
        }
    }
