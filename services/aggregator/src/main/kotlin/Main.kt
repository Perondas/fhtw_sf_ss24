package at.fhtw

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig

import org.apache.kafka.streams.kstream.*
import java.util.*

fun main(args: Array<String>) {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "aggregator"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9096"
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

    val builder = StreamsBuilder()
    val textLines: KStream<String, String> = builder.stream("meteoWeather")
    val wordCounts: KTable<String, Long> = textLines
        .flatMapValues { textLine -> listOf(*textLine.lowercase(Locale.getDefault()).split("\\W+".toRegex()).toTypedArray()) }
        .filter{_, word -> word.length > 5}
        .groupBy { _, word -> word }
        .count(Materialized.`as`("counts-store"))

    wordCounts.toStream().to("wordsLongerThanFive", Produced.with(Serdes.String(), Serdes.Long()))

    val streams = KafkaStreams(builder.build(), props)
    streams.start()
}
