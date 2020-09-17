package entities.request

data class DataPublishRequest(
    var podcast: DataPodcastRequest
)

data class DataPodcastRequest(
    var audio: DataAudio,
    var meta_data: DataMetaData
)

data class DataAudio(
    var audio_url: String,
    var original_audio_url: String?,
    var duration: Int,
    var total_samples: Double,
    var total_length: Double
)

data class DataMetaData(
    var title: String,
    var caption: String,
    var latitude: Double,
    var longitude: Double,
    var image_url: String,
    var category_id: Int
)
