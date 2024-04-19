@file:UseSerializers(ZonedDateTimeSerializer::class)

package at.fhtw.model

import at.fhtw.model.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant
import java.time.ZonedDateTime

@Serializable
data class StationGeoJSONSerializer(
    @SerialName("media_type")
    val mediaType: String? = null,
    val type: String? = null,
    val version: String,
    /* Format: *YYYY-MM-DDThh:mm:ss±hh:mm* */
    val timestamps: Array<@Serializable(with = ZonedDateTimeSerializer::class) ZonedDateTime>,
    val features: Array<StationGeoJSONFeature>,
    val filename: String? = null
)