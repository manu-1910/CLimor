package io.square1.limor.remote.entities.responses

import io.reactivex.annotations.Nullable
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWSignUpResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val data: NWData = NWData(),
    @Optional
    val message: String = ""
)

@Serializable
data class NWData(
    @Optional
    val access_token: NWAccessToken = NWAccessToken(),
    @Optional
    val user: NWUser = NWUser()
)

@Serializable
data class NWAccessToken(
    @Optional
    val token: NWToken = NWToken()
)

@Serializable
data class NWLinks(
    @Optional
    val website: ArrayList<NWWebsiteItems> = ArrayList(),
    @Optional
    val content: ArrayList<NWContentItems> = ArrayList(),
    @Optional
    val caption: ArrayList<NWCaptionItems> = ArrayList()
)

@Serializable
data class NWWebsiteItems (
    @Optional
    val id : Int = 0,
    @Optional
    val link : String = "",
    @Optional
    val start_index : Int = 0,
    @Optional
    val end_index : Int = 0
)


@Serializable
data class NWContentItems (
    @Optional
    val id : Int = 0,
    @Optional
    val link : String = "",
    @Optional
    val start_index : Int = 0,
    @Optional
    val end_index : Int = 0
)

@Serializable
data class NWCaptionItems (
    @Optional
    val id : Int = 0,
    @Optional
    val link : String = "",
    @Optional
    val start_index : Int = 0,
    @Optional
    val end_index : Int = 0
)

@Serializable
data class NWImages(
    @Optional
    val large_url: String = "",
    @Optional
    val medium_url: String = "",
    @Optional
    val original_url: String = "",
    @Optional
    val small_url: String = ""
)

@Serializable
data class NWUser(
    @Optional
    val active: Boolean = false,
    @Optional
    val autoplay_enabled: Boolean = false,
    @Optional
    val blocked: Boolean = false,
    @Optional
    val blocked_by: Boolean = false,
    @Optional
    val date_of_birth: Long? = 0,
    @Optional
    val description: String? = "",
    @Optional
    val email: String? = "",
    @Optional
    val first_name: String? = "",
    @Optional
    val followed: Boolean = false,
    @Optional
    val followed_by: Boolean = false,
    @Optional
    val followers_count: Int? = 0,
    @Optional
    val following_count: Int? = 0,
    @Optional
    val gender: String? = "",
    @Optional
    val id: Int = 0,
    @Optional
    val images: NWImages = NWImages(),
    @Optional
    val last_name: String? = "",
    @Optional
    val links: NWLinks = NWLinks(),
    @Optional
    val notifications_enabled: Boolean = false,
    @Optional
    val phone_number: String? = "",
    @Optional
    val sharing_url: String? = "",
    @Optional
    val suspended: Boolean = false,
    @Optional
    val unread_conversations: Boolean = false,
    @Optional
    val unread_conversations_count: Int? = 0,
    @Optional
    val unread_messages: Boolean = false,
    @Optional
    val unread_messages_count: Int? = 0,
    @Optional
    val username: String? = "",
    @Optional
    val verified: Boolean = false,
    @Optional
    val website: String? = ""
)
