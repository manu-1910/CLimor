package entities.response

data class PopularPodcastsResponseEntity(
    val code: Int = 0,
    val message: String = "",
    val data: PopularPodcastsArrayEntity = PopularPodcastsArrayEntity()

)

data class PopularPodcastsArrayEntity(
    val podcasts: ArrayList<PodcastEntity> = ArrayList()
)