package entities.request

data class DataLogoutRequest(
    val token: String,
    val uuid: String
)