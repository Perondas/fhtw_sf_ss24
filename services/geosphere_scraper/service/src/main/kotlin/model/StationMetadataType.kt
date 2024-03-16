package at.fhtw.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StationMetadataType {
    @SerialName("COMBINED")
    COMBINED,

    @SerialName("SUBSTATION")
    SUBSTATION,

    @SerialName("INDIVIDUAL")
    INDIVIDUAL;
}