@file:UseSerializers(LocalDateTimeSerializer::class)

package at.fhtw.model

import at.fhtw.model.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

@Serializable
data class StationGeoJSONSerializer (
    @SerialName("media_type")
    val mediaType: String? = null,
    val type: String? = null,
    val version: String,
    /* Format: *YYYY-MM-DDThh:mm:ss±hh:mm* */
    val timestamps: Array<LocalDateTime>,
    val features: Array<StationGeoJSONFeature>,
    val filename: String? = null
)