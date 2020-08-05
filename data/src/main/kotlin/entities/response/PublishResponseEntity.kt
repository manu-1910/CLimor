package entities.response


data class PublishResponseEntity(
    var code: Int,
    var data: DataPublishResponseEntity,
    var message: String
)

data class DataPublishResponseEntity(
    var podcast: PodcastEntity
)

data class PodcastEntity(
    var active: Boolean,
    var address: String?,
    var audio: AudioEntity,
    var bookmarked: Boolean,
    var caption: String,
    var created_at: Int,
    var id: Int,
    var images: ImagesEntity,
    var latitude: Double,
    var liked: Boolean,
    var links: LinksEntity,
    var listened: Boolean,
    var longitude: Double,
    var mentions: MentionsEntity,
    var number_of_comments: Int,
    var number_of_likes: Int,
    var number_of_listens: Int,
    var number_of_recasts: Int,
    var recasted: Boolean,
    var reported: Boolean,
    var saved: Boolean,
    var sharing_url: String,
    var tags: ArrayList<TagsEntity>,
    var title: String,
    var updated_at: Int,
    var user: UserEntity
)


data class AudioEntity(
    var audio_url: String,
    var duration: Int,
    var original_audio_url: String?,
    var sample_rate: Double,
    var timestamps: ArrayList<String>,
    var total_length: Double,
    var total_samples: Double
)


class MentionsEntity(
)

class TagsEntity(
    var id: Int,
    var text: String,
    var count: Int,
    var isSelected: Boolean
)

