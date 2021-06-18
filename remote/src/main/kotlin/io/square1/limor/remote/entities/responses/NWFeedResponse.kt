package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWFeedResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWFeedItemsArray = NWFeedItemsArray()
)

@Serializable
data class NWFeedItemsArray(

    val feed_items: ArrayList<NWFeedItems> = ArrayList()
)

@Serializable
data class NWFeedItems(

    val id: String = "",

    val podcast: NWPodcast? = NWPodcast(),

    val user: NWUser = NWUser(),

    val recasted: Boolean = true,

    val created_at: Int = 0


    //
    //val ad: String = "" // caution, in this example response the value returned is always null, so I don't know which value type it really is
)

