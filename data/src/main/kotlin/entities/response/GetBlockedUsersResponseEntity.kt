package entities.response

data class GetBlockedUsersResponseEntity(
    var code: Int,
    var message: String,
    var data: GetBlockedUsersDataEntity
)

data class GetBlockedUsersDataEntity(
    var blocked_users: ArrayList<UserEntity>
)