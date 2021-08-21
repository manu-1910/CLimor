package com.limor.app.uimodels

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
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
    val latitude: Double?,
    val longitude: Double?,
    var isSelected: Boolean
) : Serializable {
    constructor() : this( "", null,null, false)

    companion object {
        fun fromPlace(place: Place): UILocations {
            return UILocations(
                place.address!!,
                place.latLng?.latitude!!,
                place.latLng?.longitude!!,
                true
            )
        }
    }
}

data class UILocationsList(
    val mainText: String,
    val placeID: String
) : Serializable {
    constructor() : this( "", "")

    companion object {
        fun fromPrediction(autocompletePrediction: AutocompletePrediction): UILocationsList {
            return UILocationsList(
                autocompletePrediction.getFullText(null).toString(),
                autocompletePrediction.placeId
            )
        }
    }
}