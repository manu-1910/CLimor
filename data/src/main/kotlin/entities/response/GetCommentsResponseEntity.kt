package entities.response


data class GetCommentsResponseEntity(
    var code: Int,
    var message: String,
    var data: CommentsEntityArray
)

data class CommentsEntityArray(
    var comments: ArrayList<CommentEntity>
)

data class CommentEntity(
    var id: Int,
    var user: UserEntity?,
    var content: String?,
    var created_at: Int,
    var updated_at: Int,
    var mentions: MentionsEntity,
    var tags: TagsEntity,
    var active: Boolean,
    var audio: CommentAudioEntity,
    var type: String,
    var liked: Boolean,
    var number_of_likes: Int,
    var owner_id: Int,
    var owner_type: String,
    var comments: ArrayList<CommentEntity>,
    var podcast: PodcastEntity?,
    var number_of_listens: Int,
    var podcast_id: Int?,
    var links: LinksEntity,
    var comment_count: Int
)

data class CommentAudioEntity(
    var url: String?,
    var duration: Int?
)