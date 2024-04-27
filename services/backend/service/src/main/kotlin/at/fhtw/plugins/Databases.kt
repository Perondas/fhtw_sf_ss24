package at.fhtw.plugins

import at.fhtw.plugins.dtos.WeatherDto
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde
import io.ktor.server.application.Application
import com.fhtw.protobuf.WeatherData.Weather
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE
import java.sql.*
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import java.time.Duration

fun Application.configureDatabases() {
    val connection = connectToPostgres()
    val weatherService = WeatherService(connection)

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
        "application.id" to "backend1",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to specificWeatherSerde.javaClass.name,
        "enable.auto.commit" to "false"
    ).toProperties()

    val consumer =
        KafkaConsumer<String, Weather>(props, Serdes.String().deserializer(), specificWeatherSerde.deserializer())
    consumer.subscribe(listOf("weather-data"))

    launch {
        while (true) {
            val records = consumer.poll(Duration.ofMillis(1000))
            try {
                for (record in records) {
                    val weather = record.value()
                    weatherService.create(WeatherDto.fromProto(weather))
                    val commitEntry: Map<TopicPartition, OffsetAndMetadata> = mapOf(
                        TopicPartition(record.topic(), record.partition()) to OffsetAndMetadata(record.offset())
                    )
                    consumer.commitSync(commitEntry)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}



fun connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    val url = System.getenv("POSTGRES_URL") ?: "jdbc:postgresql://localhost:5432/db"
    val user = System.getenv("POSTGRES_USER") ?: "user"
    val password = System.getenv("POSTGRES_PASSWORD") ?: "password"

    return DriverManager.getConnection(url, user, password)
}
