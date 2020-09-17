package io.square1.limor.mappers


import entities.request.*
import io.square1.limor.uimodels.*


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
