package entities.request

data class DataChangePasswordRequest(
    val current_password: String,
    val new_password: String
)