package com.limor.app.mappers


import com.limor.app.uimodels.UILocationsRequest
import entities.request.DataSearchTermRequest


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
