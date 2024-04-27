package at.fhtw.plugins.dtos

import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val zipCode: String,
    val region: String,
    val time: Long,
    val temperature: Double,
    val humidity: Double,
    val precipitation: Double,
    val pressure: Double
) {
    companion object {
        fun fromProto(proto: com.fhtw.protobuf.WeatherData.Weather): WeatherDto {
            return WeatherDto(
                lat = proto.latitude,
                lon = proto.longitude,
                timezone = proto.timezone,
                zipCode = proto.zipCode,
                region = proto.region,
                time = proto.time,
                temperature = proto.temperature,
                humidity = proto.relativeHumidity,
                precipitation = proto.precipitation,
                pressure = proto.surfacePressure
            )
        }
    }

    fun toProto(): com.fhtw.protobuf.WeatherData.Weather {
        return com.fhtw.protobuf.WeatherData.Weather.newBuilder().setLatitude(lat).setLongitude(lon).setTime(time)
            .setRegion(region).setZipCode(zipCode).setTimezone(timezone).setTemperature(temperature)
            .setRelativeHumidity(humidity).setPrecipitation(precipitation).setSurfacePressure(pressure).build()
    }
}
