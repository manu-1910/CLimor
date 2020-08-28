package entities.response

data class CreateCommentLikeResponseEntity(
    val code: Int,
    val message: String,
    val data: CreateCommentLikeData?
)

data class CreateCommentLikeData (
    val like: CommentLikeEntity?
)

data class CommentLikeEntity (
    val comment_id : Int,
    val user_id: Int
)