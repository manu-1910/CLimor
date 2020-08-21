package entities.response

data class CreateFriendResponseEntity(
    val code : Int,
    val message: String,
    val data: FollowedEntity?
)

data class FollowedEntity (
    val followed: Boolean
)