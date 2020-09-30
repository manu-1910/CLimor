package entities.request

import kotlinx.serialization.Serializable


@Serializable
data class DataCreateReportRequestEntity (
    val reason: String?
)