package entities.response


data class SignUpResponseEntity(
    val code: Int,
    val data: DataEntity,
    val message: String
)


data class DataEntity(
    val access_token: AccessTokenEntity,
    val user: UserEntity
)


data class AccessTokenEntity(
    val token: DataTokenEntity
)


data class LinksEntity(
    val links: String
)


data class ImagesEntity(
    val large_url: String,
    val medium_url: String,
    val original_url: String,
    val small_url: String
)


data class UserEntity(
    val active: Boolean,
    val autoplay_enabled: Boolean,
    val blocked: Boolean,
    val blocked_by: Boolean,
    val date_of_birth: Int?,
    val description: String?,
    val email: String?,
    val first_name: String?,
    val followed: Boolean,
    val followed_by: Boolean,
    val followers_count: Int?,
    val following_count: Int?,
    val gender: String?,
    val id: Int,
    val images: ImagesEntity,
    val last_name: String?,
    val links: LinksEntity,
    val notifications_enabled: Boolean,
    val phone_number: String?,
    val sharing_url: String?,
    val suspended: Boolean,
    val unread_conversations: Boolean,
    val unread_conversations_count: Int?,
    val unread_messages: Boolean,
    val unread_messages_count: Int?,
    val username: String?,
    val verified: Boolean,
    val website: String?
)


data class TokenEntity(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val scope: String,
    val created_at: Long
)