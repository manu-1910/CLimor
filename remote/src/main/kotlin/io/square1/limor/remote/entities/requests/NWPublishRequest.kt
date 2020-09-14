package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWPublishRequest(
    var podcast: NWPodcastRequest = NWPodcastRequest()
)

@Serializable
data class NWPodcastRequest(
    var audio: NWAudio = NWAudio(),
    var meta_data: NWMetaData = NWMetaData()
)

@Serializable
data class NWAudio(
    var audio_url: String = "",
    var original_audio_url: String? = "",
    var duration: Int = 0,
    var total_samples: Double = 0.0,
    var total_length: Double = 0.0,
    @Optional
    var sample_rate: Double = 0.0,
    @Optional
    var timestamps: ArrayList<String> = ArrayList()
)

@Serializable
data class NWMetaData(
    var title: String = "",
    var caption: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var image_url: String = "",
    var category_id: Int = 0
)