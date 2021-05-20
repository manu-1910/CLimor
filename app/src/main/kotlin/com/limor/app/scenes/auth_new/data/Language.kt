package com.limor.app.scenes.auth_new.data

import com.limor.app.LanguagesQuery
import kotlin.random.Random

data class LanguageWrapper(val language: LanguagesQuery.Language, var isSelected: Boolean = false) {
    val name: String
        get() = language.name ?: ""
    val nativeName: String
        get() = language.native_name ?: ""
}

fun createMockedLanguages(): List<LanguageWrapper> {
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
    }.mapIndexed { i, it ->
        LanguageWrapper(
            LanguagesQuery.Language(
                code = it.toString(),
                name = categoriesNamesList[i],
                native_name = categoriesNamesList[i]
            )
        )
    }
}

fun getLanguagesByInput(input: String, languages: List<LanguageWrapper>): List<LanguageWrapper> {
    return languages.filter {
        it.name.contains(input.trim(), ignoreCase = true) ||
                it.nativeName.contains(input.trim(), ignoreCase = true)
    }
}