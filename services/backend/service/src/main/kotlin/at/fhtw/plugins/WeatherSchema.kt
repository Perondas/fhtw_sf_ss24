package at.fhtw.plugins

import at.fhtw.plugins.dtos.WeatherDto
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.Statement
import java.sql.Timestamp

class WeatherService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_WEATHER =
            "CREATE TABLE IF NOT EXISTS weather_data (\n" +
                    "    id SERIAL PRIMARY KEY,\n" +
                    "    location GEOMETRY,\n" +
                    "    timeZone VARCHAR(64),\n" +
                    "    zipCode VARCHAR(64),\n" +
                    "    region VARCHAR(64),\n" +
                    "    time TIMESTAMP,\n" +
                    "    temperature DOUBLE PRECISION,\n" +
                    "    humidity DOUBLE PRECISION,\n" +
                    "    precipitation DOUBLE PRECISION,\n" +
                    "    pressure DOUBLE PRECISION\n" +
                    ");\n" +
                    "\n" +
                    "Create index IF NOT EXISTS weather_data_location_index on weather_data using GIST(location);\n" +
                    "create index IF NOT EXISTS weather_data_time_index on weather_data(time);\n" +
                    "create index IF NOT EXISTS weather_data_zipCode_index on weather_data(zipCode);"
        private const val SELECT_BY_ZIPCODE = "SELECT * FROM weather_data WHERE zipCode = ?"
        private const val INSERT_WEATHER = "INSERT INTO weather_data (location, timeZone, zipCode, region, time, temperature, humidity, precipitation, pressure) VALUES (geometry(point(?,?)), ?, ?, ?, ?, ?, ?, ?, ?)"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_WEATHER)
    }


    suspend fun create(weater: WeatherDto): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_WEATHER, Statement.RETURN_GENERATED_KEYS)
        statement.setDouble(1, weater.lon)
        statement.setDouble(2, weater.lat)
        statement.setString(3, weater.timezone)
        statement.setString(4, weater.zipCode)
        statement.setString(5, weater.region)
        statement.setTimestamp(6, Timestamp(weater.time * 1000))
        statement.setDouble(7, weater.temperature)
        statement.setDouble(8, weater.humidity)
        statement.setDouble(9, weater.precipitation)
        statement.setDouble(10, weater.pressure)

        statement.executeUpdate()
        val resultSet = statement.generatedKeys

        if (resultSet.next()) {
            return@withContext resultSet.getInt(1)
        } else {
            throw Exception("Record not found")
        }
    }

    suspend fun getByZipCode(zipCode: String): List<WeatherDto> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_BY_ZIPCODE)
        statement.setString(1, zipCode)
        val resultSet = statement.executeQuery()
        val result = mutableListOf<WeatherDto>()
        while (resultSet.next()) {
            result.add(
                WeatherDto(
                    lat = resultSet.getDouble("location").toString().split(" ")[1].toDouble(),
                    lon = resultSet.getDouble("location").toString().split(" ")[2].toDouble(),
                    timezone = resultSet.getString("timeZone"),
                    zipCode = resultSet.getString("zipCode"),
                    region = resultSet.getString("region"),
                    time = resultSet.getTimestamp("time").time,
                    temperature = resultSet.getDouble("temperature"),
                    humidity = resultSet.getDouble("humidity"),
                    precipitation = resultSet.getDouble("precipitation"),
                    pressure = resultSet.getDouble("pressure")
                )
            )
        }
        return@withContext result
    }
}

