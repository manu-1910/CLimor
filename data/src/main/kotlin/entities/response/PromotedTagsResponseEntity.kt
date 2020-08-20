package entities.response


data class PromotedTagsResponseEntity(
    var code: Int,
    var message: String,
    var data: PromotedTagsArrayEntity
)

data class PromotedTagsArrayEntity(
    var promoted_tags: ArrayList<TagsEntity>
)
