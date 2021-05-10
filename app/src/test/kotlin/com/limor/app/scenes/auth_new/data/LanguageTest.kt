package com.limor.app.scenes.auth_new.data

import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageTest {

    @Test
    fun checkLanguagesLowerCasePositive() {
        val languages = createMockedLanguages()
        val input = "ja"
        val filtered = getLanguagesByInput(input, languages)
        print("filtered length ${filtered.size}")
        assertTrue(filtered.isNotEmpty())
    }

    @Test
    fun checkLanguagesUpperCasePositive() {
        val languages = createMockedLanguages()
        val input = "Ja"
        val filtered = getLanguagesByInput(input, languages)
        print("filtered length ${filtered.size}")
        assertTrue(filtered.isNotEmpty())
    }

    @Test
    fun checkLanguagesEmpty() {
        val languages = createMockedLanguages()
        val input = ""
        val filtered = getLanguagesByInput(input, languages)
        print("filtered length ${filtered.size}")
        assertTrue(filtered.isNotEmpty())
    }
}