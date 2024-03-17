package at.fhtw

import at.fhtw.model.GridForecastGeoJSONSerializer
import at.fhtw.model.GridForecastMetadataModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.event.Level
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val producerProps = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka1:9092",
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "security.protocol" to "PLAINTEXT"
    )

    val producer = KafkaProducer<String, String>(producerProps)

    val res = client.get("https://dataset.api.hub.geosphere.at/v1/grid/forecast/nwp-v1-1h-2500m/metadata")
    val metadata = res.body<GridForecastMetadataModel>()

    val atS = 46.37
    val atW = 9.18
    val atN = 49.02
    val atE = 17.16

    val difN = atN - atS
    val difE = atE - atW

    val setpN = difN / 10
    val setpE = difE / 10

    for (n in atS..atN step setpN) {
        for (e in atW..atE step setpE) {
            val data = client.get {
                url("https://dataset.api.hub.geosphere.at/v1/grid/forecast/nwp-v1-1h-2500m")
                for (parameter in metadata.parameters) {
                    parameter("parameters", parameter.name)
                }

                println("$n,$e,${n + setpN},${e + setpE}")
                parameter("bbox", "$n,$e,${n + setpN},${e + setpE}")
            }

            val gridForecast = data.body<GridForecastGeoJSONSerializer>()

            gridForecast.timestamps.forEachIndexed { index, forecastTime ->
                for (feature in gridForecast.features) {
                    val key = "${feature.geometry.coordinates[0]}-${feature.geometry.coordinates[1]}-$forecastTime"
                    val value = mutableMapOf<String, Map<String,String>>()

                    feature.properties.parameters.forEach {
                        value[it.key] = mapOf(
                            "value" to it.value.data[index].toString(),
                            "unit" to it.value.unit
                        )
                    }

                    val serialized = Json.encodeToString(value)

                    producer.asyncSend(
                        ProducerRecord(
                            "geosphere-forecast",
                            key,
                            serialized
                        )
                    )
                }
            }
        }
    }

    producer.close()
}

suspend fun <K, V> Producer<K, V>.asyncSend(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata, exception ->
            exception?.let(continuation::resumeWithException)
                ?: continuation.resume(metadata)
        }
    }

infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0) { "Step must be positive, was: $step." }
    val sequence = generateSequence(start) { previous ->
        if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
        val next = previous + step
        if (next > endInclusive) null else next
    }
    return sequence.asIterable()
}