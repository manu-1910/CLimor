package com.limor.app.scenes.auth_new.data

import org.json.JSONArray

data class Country(val name: String, val code: String, val codeLetters: String) {
    var emoji:String = ""

    constructor(array: JSONArray)
            : this(array.getString(0), array.getString(1), array.getString(2)){
        emoji = countryCodeToEmoji() ?:""
    }

    fun countryCodeToEmoji(): String? {

        // offset between uppercase ascii and regional indicator symbols
        var code = codeLetters
        val OFFSET = 127397

        // validate code
        if (code == null || code.length != 2) {
            return ""
        }

        //fix for uk -> gb
        if (code.equals("uk", ignoreCase = true)) {
            code = "gb"
        }

        // convert code to uppercase
        code = code.toUpperCase()
        val emojiStr = StringBuilder()

        //loop all characters
        for (i in 0 until code.length) {
            emojiStr.appendCodePoint(code[i].toInt() + OFFSET)
        }

        // return emoji
        return emojiStr.toString()
    }

    val visualFormat:String
    get() = "$emoji +$code"
}