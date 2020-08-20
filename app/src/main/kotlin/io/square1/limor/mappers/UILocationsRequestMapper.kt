package io.square1.limor.mappers


import entities.request.DataLocationsRequest
import io.square1.limor.uimodels.UILocationsRequest


//****** FROM UI TO DATA
fun UILocationsRequest.asDataEntity(): DataLocationsRequest {
    return DataLocationsRequest(
        term
    )
}


//****** FROM DATA TO UI
fun UILocationsRequest.asUIModel(): DataLocationsRequest {
    return DataLocationsRequest(
        term
    )
}
