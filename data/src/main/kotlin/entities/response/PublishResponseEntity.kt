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
    var latitude: Double?,
    var liked: Boolean,
    var links: LinksEntity,
    var listened: Boolean,
    var longitude: Double?,
    var mentions: MentionsEntity,
    var number_of_comments: Int,
    var number_of_likes: Int,
    var number_of_listens: Int,
    var number_of_recasts: Int,
    var recasted: Boolean,
    var reported: Boolean,
    var saved: Boolean,
    var sharing_url: String?,
    var tags: TagsArrayEntity,
    var title: String,
    var updated_at: Int,
    var user: UserEntity,
    var category: CategoryEntity
)


data class AudioEntity(
    var audio_url: String,
    var original_audio_url: String?,
    var duration: Int,
    var total_samples: Double,
    var total_length: Double
)


class MentionsEntity (
    var mentions : ContentMentionItemArray
)

class ContentMentionItemArray(
    var content : ArrayList<ContentMentionItemEntity>
)

class ContentMentionItemEntity(
    var user_id : Int,
    var username: String,
    var start_index: Int,
    var end_index: Int
)


class TagsEntity(
    var id: Int,
    var text: String,
    var count: Int,
    var isSelected: Boolean
)


class CategoryEntity(
    var id: Int,
    var name: String,
    var priority: Int,
    var created_at: Long,
    var updated_at: Long
)

