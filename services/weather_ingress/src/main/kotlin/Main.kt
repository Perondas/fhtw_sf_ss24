package at.fhtw

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*


suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val res = client.get("https://dataset.api.hub.geosphere.at/v1/station/current/tawes-v1-10min/metadata")
    val metadata = res.body<JsonObject>()
    val stations = metadata["stations"]?.jsonArray


    val tl = client.get {
        url("https://dataset.api.hub.geosphere.at/v1/station/current/tawes-v1-10min")
        parameter("parameters", "TL")
        for (station in stations!!) {
            val stationId = station.jsonObject["id"]?.jsonPrimitive?.content
            parameter("station_ids", stationId)
        }
    }.call.body<JsonObject>()

    val features = tl["features"]?.jsonArray

    for (property in features!!.map { it.jsonObject["properties"]?.jsonObject }) {
        val stationId = property?.get("station")?.jsonPrimitive?.content
        val temp = property?.get("parameters")?.jsonObject?.get("TL")?.jsonObject

        val stationName = metadata["stations"]?.jsonArray?.find { it.jsonObject["id"]?.jsonPrimitive?.content == stationId }?.jsonObject?.get("name")?.jsonPrimitive?.content
        println("Station $stationName has temperature $temp")
    }
}