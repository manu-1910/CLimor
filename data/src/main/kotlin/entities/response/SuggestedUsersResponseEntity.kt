package entities.response

data class SuggestedUsersResponseEntity(
    val code: Int = 0,
    val message: String = "",
    val data: UsersArrayEntity = UsersArrayEntity()

)

data class UsersArrayEntity(
    val users: ArrayList<UserEntity> = ArrayList()
)
