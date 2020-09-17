package entities.request

import kotlinx.serialization.Serializable


@Serializable
data class DataCreateUserReportRequestEntity (
    val reason: String?
)