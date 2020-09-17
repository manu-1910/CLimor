package entities.response


data class CreateBlockedUserResponseEntity(
    val code : Int,
    val message: String,
    val data: UserBlockedEntity?
)

data class UserBlockedEntity (
    val blocked: Boolean
)