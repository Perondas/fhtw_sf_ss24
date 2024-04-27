package at.fhtw

import com.fhtw.protobuf.WeatherData
import com.fhtw.protobuf.WeatherData.Weather
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import java.time.Duration
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess


const val TopicName = "weather-data"

suspend fun main() {
    val specificWeatherSerde = KafkaProtobufSerde<Weather>()
    val serdeProps = mapOf(
        SCHEMA_REGISTRY_URL_CONFIG to (System.getenv("SCHEMA_SERVER") ?: "http://localhost:8081"),
        SPECIFIC_PROTOBUF_VALUE_TYPE to Weather::class.java.name,
    )

    specificWeatherSerde.configure(serdeProps, false)


    val props = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (System.getenv("KAFKA_SERVER") ?: "localhost:9094"),
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer",
        "security.protocol" to "PLAINTEXT",
        "schema.registry.url" to (System.getenv("SCHEMA_SERVER") ?: "http://localhost:8081"),
        "application.id" to "aggregator",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to specificWeatherSerde.javaClass.name
    ).toProperties()

    val admin = AdminClient.create(props)
    if (!admin.listTopics().names().get().contains(TopicName)) {
        admin.createTopics(listOf(NewTopic(TopicName, 3, 1))).all().get()
    }
    admin.close()

    val builder = StreamsBuilder()
    val geosphereData: KStream<String, Weather> = builder.stream("geosphere-data", Consumed.with(Serdes.String(),  specificWeatherSerde))
    val openData: KStream<String, Weather> = builder.stream("open-data", Consumed.with(Serdes.String(),  specificWeatherSerde))
    val meteoData: KStream<String, Weather> = builder.stream("meteo-data", Consumed.with(Serdes.String(),  specificWeatherSerde))

    val allData = geosphereData.merge(openData).merge(meteoData)

    allData.to(TopicName, Produced.with(Serdes.String(),  specificWeatherSerde))

    val streams = KafkaStreams(builder.build(), props)

    val latch = CountDownLatch(1)

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(object : Thread("streams-shutdown-hook") {
        override fun run() {
            streams.close(Duration.ofSeconds(5))
            latch.countDown()
        }
    })

    try {
        streams.start()
        withContext(Dispatchers.IO) {
            latch.await()
        }
    } catch (e: Throwable) {
        exitProcess(1)
    }
    exitProcess(0)
}
