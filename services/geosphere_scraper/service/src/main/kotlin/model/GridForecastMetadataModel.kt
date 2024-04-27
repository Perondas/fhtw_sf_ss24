package at.fhtw.model

import at.fhtw.model.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime

@Serializable
data class GridForecastMetadataModel(
    val title: String,
    val parameters: Array<ParameterMetadataModel>,
    val frequency: String,
    val type: String,
    val mode: String,
    @SerialName("response_formats")
    val responseFormats: Array<String>,
    @SerialName("last_forecast_reftime")
    @Serializable(with = ZonedDateTimeSerializer::class)
    val lastForecastReftime: ZonedDateTime? = null,
    @SerialName("max_forecast_offset")
    val maxForecastOffset: Int? = null,
    @SerialName("available_forecast_reftimes")
    val availableForecastReftimes: Array<@Serializable(with = ZonedDateTimeSerializer::class) ZonedDateTime>,
    @SerialName("forecast_length")
    val forecastLength: Int? = null,
    val bbox: Array<Double>,
    @SerialName("bbox_outer")
    val bboxOuter: Array<Double>,
    @SerialName("spatial_resolution_m")
    val spatialResolutionM: Int,
    val crs: String,
    @SerialName("grid_bounds")
    val gridBounds: Array<Double>
)