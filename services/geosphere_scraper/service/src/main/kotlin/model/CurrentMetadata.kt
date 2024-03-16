package at.fhtw.model

import at.fhtw.model.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 
 * @param title 
 * @param parameters 
 * @param frequency 
 * @param type 
 * @param mode 
 * @param responseFormats 
 * @param time 
 * @param stations 
 * @param idType 
 */
@Serializable
data class CurrentMetadata (

    val title: String,
    val parameters: Array<ParameterMetadataModel>,
    val frequency: String,
    val type: String,
    val mode: String,
    @SerialName("response_formats")
    val responseFormats: Array<String>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val time: java.time.LocalDateTime,
    val stations: Array<StationMetadata>,
    @SerialName("id_type")
    val idType: IdType
)