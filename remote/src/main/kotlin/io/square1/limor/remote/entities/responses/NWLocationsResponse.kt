package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWLocationsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWLocationsArray = NWLocationsArray()
)

@Serializable
data class NWLocationsArray(
    @Optional
    val locations: ArrayList<NWLocations> = ArrayList()
)

@Serializable
data class NWLocations(
    @Optional
    val address: String = "",
    @Optional
    val latitude: Double = 0.0,
    @Optional
    val longitude: Double = 0.0,
    @Optional
    val isSelected: Boolean = false
)
