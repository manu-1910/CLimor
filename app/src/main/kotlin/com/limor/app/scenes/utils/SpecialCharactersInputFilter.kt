package com.limor.app.scenes.utils

import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils


class SpecialCharactersInputFilter : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var keepOriginal = true
        val sb = StringBuilder(end - start)
        for (i in start until end) {
            val c = source[i]
            if (isCharAllowed(c))
                sb.append(c) else keepOriginal = false
        }
        return if (keepOriginal) null else {
            if (source is Spanned) {
                val sp = SpannableString(sb)
                TextUtils.copySpansFrom(source, start, sb.length, null, sp, 0)
                sp
            } else {
                sb
            }
        }
    }

    private fun isCharAllowed(c: Char): Boolean {
        return Character.isLetterOrDigit(c) || Character.isSpaceChar(c)
    }
}

class AlphabetsInputFilter : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var keepOriginal = true
        val sb = StringBuilder(end - start)
        for (i in start until end) {
            val c = source[i]
            if (Character.isLetter(c))
                sb.append(c) else keepOriginal = false
        }
        return if (keepOriginal) null else {
            if (source is Spanned) {
                val sp = SpannableString(sb)
                TextUtils.copySpansFrom(source, start, sb.length, null, sp, 0)
                sp
            } else {
                sb
            }
        }
    }

}