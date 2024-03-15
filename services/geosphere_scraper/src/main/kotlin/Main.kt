package at.fhtw

import at.fhtw.model.GridForecastGeoJSONSerializer
import at.fhtw.model.GridForecastMetadataModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

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

            println(gridForecast)
        }
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