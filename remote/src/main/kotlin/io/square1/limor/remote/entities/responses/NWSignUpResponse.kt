package io.square1.limor.remote.entities.responses

import io.reactivex.annotations.Nullable

import kotlinx.serialization.Serializable

@Serializable
data class NWSignUpResponse(

    val code: Int = 0,

    val data: NWData = NWData(),

    val message: String = ""
)

@Serializable
data class NWData(

    val access_token: NWAccessToken = NWAccessToken(),

    val user: NWUser = NWUser()
)

@Serializable
data class NWAccessToken(

    val token: NWToken = NWToken()
)

@Serializable
data class NWLinks(

    val website: ArrayList<NWWebsiteItems> = ArrayList(),

    val content: ArrayList<NWContentItems> = ArrayList(),

    val caption: ArrayList<NWCaptionItems> = ArrayList()
)

@Serializable
data class NWWebsiteItems (

    val id : Int = 0,

    val link : String = "",

    val start_index : Int = 0,

    val end_index : Int = 0
)


@Serializable
data class NWContentItems (

    val id : Int = 0,

    val link : String = "",

    val start_index : Int = 0,

    val end_index : Int = 0
)

@Serializable
data class NWCaptionItems (

    val id : Int = 0,

    val link : String = "",

    val start_index : Int = 0,

    val end_index : Int = 0
)

@Serializable
data class NWImages(

    val large_url: String = "",

    val medium_url: String = "",

    val original_url: String = "",

    val small_url: String = ""
)

@Serializable
data class NWUser(

    val active: Boolean = false,

    val autoplay_enabled: Boolean = false,

    val blocked: Boolean = false,

    val blocked_by: Boolean = false,

    val date_of_birth: Long? = 0,

    val description: String? = "",

    val email: String? = "",

    val first_name: String? = "",

    val followed: Boolean = false,

    val followed_by: Boolean = false,

    val followers_count: Int? = 0,

    val following_count: Int? = 0,

    val gender: String? = "",

    val id: Int = 0,

    val images: NWImages = NWImages(),

    val last_name: String? = "",

    val links: NWLinks = NWLinks(),

    val notifications_enabled: Boolean = false,

    val phone_number: String? = "",

    val sharing_url: String? = "",

    val suspended: Boolean = false,

    val unread_conversations: Boolean = false,

    val unread_conversations_count: Int? = 0,

    val unread_messages: Boolean = false,

    val unread_messages_count: Int? = 0,

    val username: String? = "",

    val verified: Boolean = false,

    val website: String? = ""
)
