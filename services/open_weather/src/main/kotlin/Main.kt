package org.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.example.model.HourlyForecastResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.example.ExcelHandler as ExcelHandler
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
    var baseURL = "https://api.openweathermap.org/data/"
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

//    val producerProperties = mapOf<String, String>(
//        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
//        "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
//        "security.protocol" to "PLAINTEXT"
//    )


//    //TODO: do it for each
//    val tl = client.get {
//        url(apiCurrentForecastURL)
//        parameter("lat", "48.21451155")
//        parameter("lon", "16.52368505")
//        parameter("appid", apiKey)
//        parameter("units", "metric")
//    }.call.body<JsonObject>()
//
//    val list = tl["list"]?.jsonArray
//
//    for (property in list!!.map { it.jsonObject }) {
//        val dateTime = property.get("dt_txt")?.jsonPrimitive?.content
//        val temp = property.get("main")?.jsonObject?.get("temp")?.jsonPrimitive?.content
//
//        println("At $dateTime the temp was $temp")
//    }

//    val producerProperties = mapOf<String, String>(
//        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
//        "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
//        "security.protocol" to "PLAINTEXT"
//    )
//
//    val producer = KafkaProducer<String, ByteArray>(producerProperties)
//

//
//    producer.use {
//        it.send(ProducerRecord("test", "1", "Hello, world!".encodeToByteArray()))
//    }
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