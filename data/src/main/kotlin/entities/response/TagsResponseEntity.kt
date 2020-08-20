package entities.response


data class TagsResponseEntity(
    var code: Int,
    var message: String,
    var data: TagsArrayEntity
)

data class TagsArrayEntity(
    var tags: ArrayList<TagsEntity>
)
