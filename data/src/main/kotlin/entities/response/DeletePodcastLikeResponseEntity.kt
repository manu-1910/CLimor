package entities.response

data class DeletePodcastLikeResponseEntity(
    val code: Int,
    val message: String,
    val data: DeletePodcastLikeData?
)

data class DeletePodcastLikeData (
    val destroyed: Boolean?
)