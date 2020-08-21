package entities.response

data class CreatePodcastLikeResponseEntity(
    val code: Int,
    val message: String,
    val data: CreatePodcastLikeData?
)

data class CreatePodcastLikeData (
    val like: PodcastLikeEntity?
)

data class PodcastLikeEntity (
    val podcast_id : Int,
    val user_id: Int
)