package entities.response

data class CreateCommentResponseEntity(
    val code: Int,
    val message: String,
    val data: CreateCommentData?
)

data class CreateCommentData (
    val comment : CommentEntity
)