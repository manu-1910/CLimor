package com.limor.app.scenes.auth_new.data

import kotlin.random.Random

data class Category(val id: Int, val name: String, var isSelected: Boolean = false)

fun createMockedCategories(): List<Category> {
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
    }.mapIndexed { i, it -> Category(it, categoriesNamesList[i]) }

}