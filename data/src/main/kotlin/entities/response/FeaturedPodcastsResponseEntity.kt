package entities.response


data class FeaturedPodcastsResponseEntity(
    val code: Int = 0,
    val message: String = "",
    val data: FeaturedPodcastsArrayEntity = FeaturedPodcastsArrayEntity()

)

data class FeaturedPodcastsArrayEntity(
    val podcasts: ArrayList<PodcastEntity> = ArrayList()
)