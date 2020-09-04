package entities.response


data class CategoriesResponseEntity(
    var code: Int,
    var message: String,
    var data: CategoriesArrayEntity
)

data class CategoriesArrayEntity(
    var categories: ArrayList<CategoryEntity>
)
