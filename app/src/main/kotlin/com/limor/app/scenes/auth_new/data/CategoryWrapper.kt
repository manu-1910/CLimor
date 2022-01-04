package com.limor.app.scenes.auth_new.data

import com.limor.app.CategoriesQuery
import com.limor.app.uimodels.PatronCategoryUIModel
import kotlin.random.Random

data class CategoryWrapper(
    val queryCategory: CategoriesQuery.Category,
    var isSelected: Boolean = false
) {
    val name: String get() = queryCategory.name!!
    val categoryId: Int? get() = queryCategory.id
}

fun CategoryWrapper.transform(): PatronCategoryUIModel{
    return PatronCategoryUIModel(id = queryCategory.id, slug = queryCategory.slug, name = queryCategory.name, priority = 0, selected = isSelected)
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