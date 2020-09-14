package entities.request

import kotlinx.serialization.Serializable

@Serializable
data class DataUserIDRequest (
    val user_id: Int
)