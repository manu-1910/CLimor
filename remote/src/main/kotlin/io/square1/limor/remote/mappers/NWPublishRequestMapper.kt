package io.square1.limor.remote.mappers


import entities.request.*
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.*

//***** FROM REMOTE TO DATA
fun Single<NWPublishRequest>.asDataEntity(): Single<DataPublishRequest> {
    return this.map { it.asDataEntity() }
}

fun NWPublishRequest.asDataEntity(): DataPublishRequest {
    return DataPublishRequest(
        podcast.asDataEntity()
    )
}


fun NWPodcastRequest.asDataEntity(): DataPodcastRequest {
    return DataPodcastRequest(
        audio.asDataEntity(),
        meta_data.asDataEntity()
    )
}

fun NWAudio.asDataEntity(): DataAudio {
    return DataAudio(
        audio_url,
        original_audio_url,
        duration,
        total_samples,
        total_length,
        sample_rate,
        timestamps
    )
}

fun NWMetaData.asDataEntity(): DataMetaData {
    return DataMetaData(
        title,
        caption,
        latitude,
        longitude,
        image_url,
        category_id
    )
}


//***** FROM DATA TO REMOTE
fun Single<DataPublishRequest>.asRemoteEntity(): Single<NWPublishRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataPublishRequest.asRemoteEntity() : NWPublishRequest {
    return NWPublishRequest(
        podcast.asRemoteEntity()
    )
}

fun DataPodcastRequest.asRemoteEntity() : NWPodcastRequest {
    return NWPodcastRequest(
        audio.asRemoteEntity(),
        meta_data.asRemoteEntity()
    )
}

fun DataAudio.asRemoteEntity() : NWAudio {
    return NWAudio(
        audio_url,
        original_audio_url,
        duration,
        total_samples,
        total_length,
        sample_rate,
        timestamps
    )
}

fun DataMetaData.asRemoteEntity() : NWMetaData {
    return NWMetaData(
        title,
        caption,
        latitude,
        longitude,
        image_url
    )
}