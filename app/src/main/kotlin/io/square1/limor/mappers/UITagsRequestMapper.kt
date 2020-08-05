package io.square1.limor.mappers


import entities.request.*
import io.square1.limor.uimodels.*


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
