package entities.request


data class DataUpdateProfileRequest(
    val user: UpdateUserEntity
)

data class UpdateUserEntity(
    val first_name: String?,
    val last_name: String?,
    val username: String?,
    val website: String?,
    val description: String?,
    val email: String?,
    val phone_number: String?,
    val date_of_birth: Int?,
    val gender: String?,
    val notifications_enabled: Boolean?,
    val image_url: String?
)
