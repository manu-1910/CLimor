package com.limor.app.scenes.auth_new.data

import org.json.JSONArray

data class Country(val name: String = "", val code: String = "", val codeLetters: String = "") {
    var emoji: String = ""

    constructor(array: JSONArray)
            : this(array.getString(0), array.getString(1), array.getString(2)) {
        emoji = countryCodeToEmoji()
    }

    val isEmpty: Boolean
        get() = name.isEmpty() or code.isEmpty() or codeLetters.isEmpty()

    private fun countryCodeToEmoji(): String{
        var code = codeLetters
        val offset = 127397
        if (code.length != 2) {
            return ""
        }

        if (code.equals("uk", ignoreCase = true)) {
            code = "gb"
        }

        code = code.toUpperCase()
        val emojiStr = StringBuilder()

        for (element in code) {
            emojiStr.appendCodePoint(element.toInt() + offset)
        }

        return emojiStr.toString()
    }

    val visualFormat: String
        get() = "$emoji +$code"
}