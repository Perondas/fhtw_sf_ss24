package at.fhwt.model

import at.fhtw.model.GeoJSONFeature
import at.fhtw.model.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime

@Serializable
data class TimeseriesForecastGeoJSONSerializer(
    @Serializable(with = ZonedDateTimeSerializer::class)
    @SerialName("reference_time")
    val referenceTime: ZonedDateTime,
    @SerialName("media_type")
    val mediaType: String? = null,
    val type: String? = null,
    val version: String,
    val timestamps: Array<@Serializable(with = ZonedDateTimeSerializer::class) ZonedDateTime>,
    val features: Array<GeoJSONFeature>,
    val filename: String? = null
)