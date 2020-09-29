package io.square1.limor.mappers


import entities.request.DataSearchTermRequest
import io.square1.limor.uimodels.UILocationsRequest


//****** FROM UI TO DATA
fun UILocationsRequest.asDataEntity(): DataSearchTermRequest {
    return DataSearchTermRequest(
        term
    )
}


//****** FROM DATA TO UI
fun UILocationsRequest.asUIModel(): DataSearchTermRequest {
    return DataSearchTermRequest(
        term
    )
}
