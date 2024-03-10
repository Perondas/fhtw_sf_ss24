package at.fhtw.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class State {
    @SerialName("Burgenland")
    BURGENLAND,

    @SerialName("Kärnten")
    KÄRNTEN,

    @SerialName("Niederösterreich")
    NIEDERÖSTERREICH,

    @SerialName("Oberösterreich")
    OBERÖSTERREICH,

    @SerialName("Salzburg")
    SALZBURG,

    @SerialName("Steiermark")
    STEIERMARK,

    @SerialName("Tirol")
    TIROL,

    @SerialName("Vorarlberg")
    VORARLBERG,

    @SerialName("Wien")
    WIEN;
}