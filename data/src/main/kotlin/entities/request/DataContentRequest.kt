package entities.request

import kotlinx.serialization.Serializable


@Serializable
data class DataContentRequest (
    val content: String?
)