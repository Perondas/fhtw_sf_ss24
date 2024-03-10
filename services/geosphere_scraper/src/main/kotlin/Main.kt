package at.fhtw

import at.fhtw.model.CurrentMetadata
import at.fhtw.model.StationGeoJSONSerializer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
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

    val res = client.get("https://dataset.api.hub.geosphere.at/v1/station/current/tawes-v1-10min/metadata")
    val metadata = res.body<CurrentMetadata>()


    val data = client.get {
        url("https://dataset.api.hub.geosphere.at/v1/station/current/tawes-v1-10min")
        for (parameter in metadata.parameters) {
            parameter("parameters", parameter.name)
        }
        for (station in metadata.stations) {
            val stationId = station.id
            parameter("station_ids", stationId)
        }
    }.call

    val geoJSON = data.body<StationGeoJSONSerializer>()
    println(geoJSON)
}