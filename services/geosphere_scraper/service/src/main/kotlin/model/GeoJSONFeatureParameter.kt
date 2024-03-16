package at.fhtw.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class GeoJSONFeatureParameter (

    val name: String,
    val unit: String,
    val `data`: Array<Double?>
)