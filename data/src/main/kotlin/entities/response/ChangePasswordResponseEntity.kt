package entities.response


data class ChangePasswordResponseEntity(
    var code: Int,
    var message: String,
    var data: DataTokenEntity
)
