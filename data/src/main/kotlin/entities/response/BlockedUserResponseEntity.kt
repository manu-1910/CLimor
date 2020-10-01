package entities.response


data class BlockedUserResponseEntity(
    val code : Int,
    val message: String,
    val data: UserBlockedEntity
)

data class UserBlockedEntity (
    val blocked: Boolean
)