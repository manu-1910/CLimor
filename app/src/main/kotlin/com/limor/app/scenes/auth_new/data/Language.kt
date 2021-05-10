package com.limor.app.scenes.auth_new.data

import kotlin.random.Random

data class Language(val id: Int, val name: String, var isSelected: Boolean = false)

fun createMockedLanguages(): List<Language> {
    val categoriesNamesList = listOf(
        "English",
        "Arabic",
        "French",
        "Urdu",
        "Spanish",
        "Bengali",
        "Irish",
        "Hindi",
        "Russian",
        "German",
        "Portuguese",
        "Malay",
        "Italian",
        "Chinese",
        "Japanese",
        "Lahnda",
        "Javanese",
        "Korean",
        "Telugu",
        "Marathi",
        "Turkish",
        "Tamil",
        "Vietnamese",
        "Indonesian"
    )
    return List(categoriesNamesList.size) {
        Random.nextInt(0, 100)
    }.mapIndexed { i, it -> Language(it, categoriesNamesList[i]) }
}

fun getLanguagesByInput(input: String, languages: List<Language>): List<Language> {
    return languages.filter { it.name.contains(input.trim(), ignoreCase = true) }
}