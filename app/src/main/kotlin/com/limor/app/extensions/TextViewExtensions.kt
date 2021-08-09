package com.limor.app.extensions

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.limor.app.R

val userMentionPattern = "@\\w\\S*\\b".toRegex()

fun TextView.highlight(regex: Regex, colorResId: Int) {
    val color = ContextCompat.getColor(this.context, colorResId)
    val text = this.text.toString()
    val results = regex.findAll(text)

    val spannable = SpannableString(text)
    for (matchResult in results) {
        spannable.setSpan(ForegroundColorSpan(color), matchResult.range.first, matchResult.range.last + 1, 0)
    }
    this.text = spannable
}