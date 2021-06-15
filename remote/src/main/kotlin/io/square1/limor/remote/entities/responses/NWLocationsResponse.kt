package io.square1.limor.remote.entities.responses



import kotlinx.serialization.Serializable


@Serializable
data class NWLocationsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWLocationsArray = NWLocationsArray()
)

@Serializable
data class NWLocationsArray(

    val locations: ArrayList<NWLocations>? = ArrayList()
)

@Serializable
data class NWLocations(

    val address: String = "",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val isSelected: Boolean = false
)
