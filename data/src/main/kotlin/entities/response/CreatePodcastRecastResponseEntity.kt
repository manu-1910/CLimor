package entities.response

data class CreatePodcastRecastResponseEntity(
    val code: Int,
    val message: String,
    val data: CreatePodcastRecastData?
)

data class CreatePodcastRecastData (
    val recast: PodcastRecastEntity?
)

data class PodcastRecastEntity (
    val podcast_id : Int,
    val user_id: Int
)