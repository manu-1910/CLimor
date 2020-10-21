package com.limor.app.mappers


import com.limor.app.uimodels.UITagsRequest
import entities.request.DataTagsRequest


//****** FROM UI TO DATA
fun UITagsRequest.asDataEntity(): DataTagsRequest {
    return DataTagsRequest(
        tag
    )
}


//****** FROM DATA TO UI
fun UITagsRequest.asUIModel(): DataTagsRequest {
    return DataTagsRequest(
        tag
    )
}
