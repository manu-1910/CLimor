package com.limor.app.mappers


import com.limor.app.uimodels.UIAudio
import com.limor.app.uimodels.UIMetaData
import com.limor.app.uimodels.UIPodcastRequest
import com.limor.app.uimodels.UIPublishRequest
import entities.request.DataAudio
import entities.request.DataMetaData
import entities.request.DataPodcastRequest
import entities.request.DataPublishRequest


//****** FROM UI TO DATA
fun UIPublishRequest.asDataEntity(): DataPublishRequest {
    return DataPublishRequest(
        podcast!!.asDataEntity()
    )
}


fun UIPodcastRequest.asDataEntity(): DataPodcastRequest {
    return DataPodcastRequest(
        audio.asDataEntity(),
        meta_data.asDataEntity()
    )
}

fun UIAudio.asDataEntity(): DataAudio {
    return DataAudio(
        audio_url,
        original_audio_url,
        duration,
        total_samples,
        total_length
    )
}

fun UIMetaData.asDataEntity(): DataMetaData{
    return DataMetaData(
        title,
        caption,
        latitude,
        longitude,
        image_url,
        category_id
    )
}

//****** FROM DATA TO UI

fun UIPublishRequest.asUIModel(): DataPublishRequest {
    return DataPublishRequest(
        podcast!!.asUIModel()
    )
}

fun UIPodcastRequest.asUIModel(): DataPodcastRequest {
    return DataPodcastRequest(
        audio.asUIModel(),
        meta_data.asUIModel()
    )
}





fun UIAudio.asUIModel(): DataAudio {
    return DataAudio(
        audio_url,
        original_audio_url,
        duration,
        total_samples,
        total_length
    )
}

fun UIMetaData.asUIModel(): DataMetaData {
    return DataMetaData(
        title,
        caption,
        latitude,
        longitude,
        image_url,
        category_id
    )
}



//fun Single<DataPublishRequest>.asUIModel(): Single<UIPublishRequest> {
//    return this.map { it.asUIModel() }
//}
