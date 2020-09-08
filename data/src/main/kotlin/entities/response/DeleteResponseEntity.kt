package entities.response

data class DeleteResponseEntity(
    val code: Int,
    val message: String,
    val data: DeleteData?
)

data class DeleteData (
    val destroyed: Boolean?
)