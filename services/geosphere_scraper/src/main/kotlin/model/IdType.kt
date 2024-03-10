package at.fhtw.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IdType(){
                   @SerialName("Synop")
    SYNOP,
        @SerialName("Klima")
    KLIMA;
}