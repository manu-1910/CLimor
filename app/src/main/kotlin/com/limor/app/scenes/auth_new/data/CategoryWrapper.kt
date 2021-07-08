package com.limor.app.scenes.auth_new.data

import com.limor.app.CategoriesQuery
import kotlin.random.Random

data class CategoryWrapper(
    val queryCategory: CategoriesQuery.Category,
    var isSelected: Boolean = false
) {
    val name: String get() = queryCategory.name!!
    val categoryId: Int? get() = queryCategory.id
}

fun createMockedCategories(): List<CategoryWrapper> {
    val categoriesNamesList = listOf(
        "Sport",
        "News",
        "Gaming",
        "Travel",
        "Voice",
        "Food",
        "Limor",
        "Education",
        "Health",
        "Beauty",
        "art",
        "Podcast",
        "Politics",
        "Social audio",
        "Makeup",
        "VIP",
        "Sport",
        "News",
        "Gaming",
        "Travel",
        "Voice",
        "Food",
        "Limor",
        "Education",
        "Health",
        "Beauty",
        "art",
        "Podcast",
        "Politics",
        "Social audio",
        "Makeup",
        "VIP"
    )
    return List(categoriesNamesList.size) {
        Random.nextInt(0, 100)
    }.mapIndexed { i, it ->
        CategoryWrapper(
            CategoriesQuery.Category(
                id = it,
                name = categoriesNamesList[i],
                slug = categoriesNamesList[i]
            )
        )
    }

}