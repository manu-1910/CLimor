package entities.response

data class GetUserResponseEntity(
    var code: Int,
    var message: String,
    var data: GetUserEntity
)

data class GetUserEntity(
    var user: UserEntity
)
