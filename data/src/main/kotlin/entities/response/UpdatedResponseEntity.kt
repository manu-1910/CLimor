package entities.response

data class UpdatedResponseEntity(
    val code: Int,
    val message: String,
    val data: UpdatedData?
)

data class UpdatedData (
    val updated: Boolean
)