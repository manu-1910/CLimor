package io.square1.limor.uimodels


data class UILocationsResponse(
    val code: Int,
    val message: String,
    val data: UILocationsArray
)

data class UILocationsArray(
    val locations: ArrayList<UILocations>
)

class UILocations(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    var isSelected: Boolean
)
