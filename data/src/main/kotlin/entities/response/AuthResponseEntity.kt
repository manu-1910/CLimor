package entities.response


data class AuthResponseEntity(
    val code: Int = 0,
    val message: String = "",
    val data: DataAuthResponseEntity = DataAuthResponseEntity()

)

data class DataAuthResponseEntity(
    val token: DataTokenEntity = DataTokenEntity()
)

data class DataTokenEntity(
    val access_token: String = "",
    val token_type: String = "",
    val expires_in: Long = 0,
    val scope: String = "",
    val created_at: Long = 0
)
