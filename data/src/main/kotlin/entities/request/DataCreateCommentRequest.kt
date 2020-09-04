package entities.request

data class DataCreateCommentRequest (
    val comment: DataCommentRequest
)

data class DataCommentRequest (
    val content: String,
    val duration: Int?,
    val audio_url: String?
)