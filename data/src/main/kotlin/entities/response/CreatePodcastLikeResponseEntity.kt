package entities.response

data class CreatePodcastLikeResponseEntity(
    val code: Int,
    val message: String,
    val data: DataPodcastLikeEntity?
)

data class DataPodcastLikeEntity (
    val like: PodcastLikeEntity?
)

data class PodcastLikeEntity (
    val podcast_id : Int,
    val user_id: Int
)