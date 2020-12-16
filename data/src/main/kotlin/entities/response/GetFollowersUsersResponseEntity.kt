package entities.response

data class GetFollowersUsersResponseEntity(
    var code: Int,
    var message: String,
    var data: GetFollowersUsersDataEntity
)

data class GetFollowersUsersDataEntity(
    var following_users: ArrayList<UserEntity>
)