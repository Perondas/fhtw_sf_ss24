package at.fhtw

import at.fhwt.api.CurrentApi
import at.fhwt.api.MetadataApi
import at.fhwt.model.Bundesland
import at.fhwt.model.StationCurrentMetadataModel


fun main() {
    val meta = MetadataApi().currentStationMetadataStationCurrentResourceIdMetadataGet("tawes-v1-10min")

    println(meta)
}