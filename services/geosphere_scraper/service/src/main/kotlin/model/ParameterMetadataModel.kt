package at.fhtw.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParameterMetadataModel (

    val name: String,
    @SerialName("long_name")
    val longName: String,
    val desc: String,
    val unit: String
)