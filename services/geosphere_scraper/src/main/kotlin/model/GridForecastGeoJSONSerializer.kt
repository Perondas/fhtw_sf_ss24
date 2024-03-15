package at.fhtw.model

import at.fhtw.model.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class GridForecastGeoJSONSerializer(
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("reference_time")
    val referenceTime: LocalDateTime,
    @SerialName("media_type")
    val mediaType: String? = null,
    val type: String? = null,
    val version: String,
    /* Format: *YYYY-MM-DDThh:mm:ss±hh:mm* */
    val timestamps: Array<@Serializable(with = LocalDateTimeSerializer::class) LocalDateTime>,
    val features: Array<GeoJSONFeature>,
    val filename: String? = null,
    val bbox: Array<Double>
)