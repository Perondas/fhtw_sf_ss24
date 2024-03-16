package at.fhtw.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoJSONFeature (
    val type: kotlin.String? = null,
    val geometry: GeoJSONPoint,
    val properties: GeoJSONFeatureProperties
)