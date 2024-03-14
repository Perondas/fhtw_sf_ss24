package at.fhtw

import at.fhwt.api.MetadataApi

fun main() {
    val meta = MetadataApi().currentStationMetadataStationCurrentResourceIdMetadataGet("tawes-v1-10min")

    println(meta)
}