package at.fhtw.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoJSONFeatureProperties(
    val parameters: Map<String, GeoJSONFeatureParameter>
)