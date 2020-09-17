package entities.response

data class CreateDeleteFriendResponseEntity(
    val code : Int,
    val message: String,
    val data: FollowedEntity?
)

data class FollowedEntity (
    val followed: Boolean
)