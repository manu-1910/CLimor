package entities.response

data class GetFollowingsUsersResponseEntity(
    var code: Int,
    var message: String,
    var data: GetFollowingsUsersDataEntity
)

data class GetFollowingsUsersDataEntity(
    var followed_users: ArrayList<UserEntity>
)