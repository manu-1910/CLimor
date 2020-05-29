package entities.request

data class DataSignUpRequest(
    val client_id: String,
    val client_secret: String,
    val scopes: String = "user",
    val user: DataSignUpUser
)

data class DataSignUpUser(
    val email: String,
    val password: String,
    val username: String
)