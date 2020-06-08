package entities.request

data class DataTokenFBRequest(
    val client_id: String,
    val client_secret: String,
    val grant_type: String,
    val facebook_access_token: String,
    val referral_code: String,
    val user: DataSignUpUser
)
