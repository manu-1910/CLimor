package com.limor.app.uimodels

import java.io.Serializable


data class UIPublishRequest(
    var podcast: UIPodcastRequest?
)

data class UIPodcastRequest(
    var audio: UIAudio,
    var meta_data: UIMetaData?
)

data class UIAudio(
    var audio_url: String,
    var original_audio_url: String?,
    var duration: Int,
    var total_samples: Double,
    var total_length: Double
): Serializable

data class UIMetaData(
    var title: String,
    var caption: String,
    var latitude: Double,
    var longitude: Double,
    var image_url: String,
    var category_id: Int
): Serializable
