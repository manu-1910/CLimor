package entities.request


data class DataMergeFacebookAccountRequest(
    val facebook_uid: String,
    val facebook_access_token: String
)
