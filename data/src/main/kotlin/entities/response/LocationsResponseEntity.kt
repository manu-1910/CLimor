package entities.response

data class LocationsResponseEntity(
    var code: Int,
    var message: String,
    var data: LocationsEntityArray
)

data class LocationsEntityArray(
    var locations: ArrayList<LocationsEntity>
)

class LocationsEntity(
    var address: String,
    var latitude: Double,
    var longitude: Double,
    var isSelected: Boolean
)
