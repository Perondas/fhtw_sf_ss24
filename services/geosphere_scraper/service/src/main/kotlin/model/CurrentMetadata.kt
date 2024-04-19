package at.fhtw.model

import at.fhtw.model.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class CurrentMetadata (
    val title: String,
    val parameters: Array<ParameterMetadataModel>,
    val frequency: String,
    val type: String,
    val mode: String,
    @SerialName("response_formats")
    val responseFormats: Array<String>,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val time: ZonedDateTime,
    val stations: Array<StationMetadata>,
    @SerialName("id_type")
    val idType: IdType
)