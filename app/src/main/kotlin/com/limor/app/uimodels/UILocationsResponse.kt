package com.limor.app.uimodels

import java.io.Serializable


data class UILocationsResponse(
    val code: Int,
    val message: String,
    val data: UILocationsArray
)

data class UILocationsArray(
    val locations: ArrayList<UILocations>
)

data class UILocations(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    var isSelected: Boolean
) : Serializable {
    constructor() : this( "", 0.0,0.0, false)
}