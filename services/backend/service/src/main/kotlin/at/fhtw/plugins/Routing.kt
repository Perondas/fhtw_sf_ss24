package at.fhtw.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Location(val zipCode: String, val name: String)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("Hello, World!")
        }

        get("/api/locations") {
            val csv =
                object {}.javaClass.getResourceAsStream("/short-plz-coord-austria.csv")?.bufferedReader()?.readText()
                    ?: throw IllegalStateException("Could not read CSV file")

            val locations = csv.split('\n').map { it.trim() }.filter { it.isNotBlank() }.map {
                val parts = it.split(';')
                Location(parts[0], parts[1])
            }

            call.respond(locations)
        }

        get("/api/weather/{zipCode}") {
            val zipCode = call.parameters["zipCode"]
            if (zipCode.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "ZipCode cannot be null or empty")
            } else {
                val connection = connectToPostgres()
                call.respond(WeatherService(connection).getByZipCode(zipCode))
            }
        }
    }
}