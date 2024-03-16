package at.fhtw.model

import kotlinx.serialization.Serializable

@Serializable
data class StationGeoJSONFeature (

    val type: kotlin.String? = null,
    val geometry: GeoJSONPoint,
    val properties: StationGeoJSONProperties
)