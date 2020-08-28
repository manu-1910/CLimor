package entities.response

data class DeleteLikeResponseEntity(
    val code: Int,
    val message: String,
    val data: DeleteLikeData?
)

data class DeleteLikeData (
    val destroyed: Boolean?
)