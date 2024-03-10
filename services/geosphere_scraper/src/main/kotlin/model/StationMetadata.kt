package at.fhtw.model

import at.fhtw.model.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StationMetadata (
    val type: StationMetadataType,
    val id: String,
    @SerialName("group_id")
    val groupId: String? = null,
    val name: String,
    val state: State? = null,
    val lat: Double,
    val lon: Double,
    val altitude: Double? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("valid_from")
    val validFrom: java.time.LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("valid_to")
    val validTo: java.time.LocalDateTime,
    @SerialName("has_sunshine")
    val hasSunshine: Boolean? = null,
    @SerialName("has_global_radiation")
    val hasGlobalRadiation: Boolean? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)