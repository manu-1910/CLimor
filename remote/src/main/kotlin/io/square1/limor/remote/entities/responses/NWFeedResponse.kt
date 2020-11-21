package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWFeedResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWFeedItemsArray = NWFeedItemsArray()
)

@Serializable
data class NWFeedItemsArray(
    @Optional
    val feed_items: ArrayList<NWFeedItems> = ArrayList()
)

@Serializable
data class NWFeedItems(
    @Optional
    val id: String = "",
    @Optional
    val podcast: NWPodcast? = NWPodcast(),
    @Optional
    val user: NWUser = NWUser(),
    @Optional
    val recasted: Boolean = true,
    @Optional
    val created_at: Int = 0


    //@Optional
    //val ad: String = "" // caution, in this example response the value returned is always null, so I don't know which value type it really is
)

