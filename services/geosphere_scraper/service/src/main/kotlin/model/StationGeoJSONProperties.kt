package at.fhtw.model

import kotlinx.serialization.Serializable

@Serializable
data class StationGeoJSONProperties (

    val parameters: Map<String, GeoJSONFeatureParameter>,
    val station: String
)