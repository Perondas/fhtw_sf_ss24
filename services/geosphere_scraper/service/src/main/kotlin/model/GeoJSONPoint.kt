package at.fhtw.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoJSONPoint (

    val type: String? = null,
    /* Point coordinates are in x, y order (easting, northing for projected coordinates, longitude, and latitude for geographic coordinates) */
    val coordinates: Array<Double>
)