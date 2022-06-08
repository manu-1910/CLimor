package com.limor.app.extensions

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.uimodels.MentionUIModel
import com.limor.app.uimodels.TagUIModel

val userMentionPattern = "@\\w\\S*\\b".toRegex()

fun TextView.highlight(regex: Regex, colorResId: Int) {
    val color = ContextCompat.getColor(this.context, colorResId)
    val text = this.text.toString()
    val results = regex.findAll(text)

    val spannable = SpannableString(text)
    for (matchResult in results) {
        spannable.setSpan(
            ForegroundColorSpan(color),
            matchResult.range.first,
            matchResult.range.last + 1,
            0
        )
    }
    this.text = spannable
}

fun TextView.setHighlighted(text: String, textToHighlight: String, colorValue: Int) {
    val spanned = SpannableString(text)
    textToHighlight
        .toRegex(RegexOption.IGNORE_CASE)
        .findAll(text)
        .map { it.range }
        .filter { it.last + 1 <= text.length }
        .forEach {
            spanned.setSpan(
                ForegroundColorSpan(colorValue),
                it.first,
                it.last + 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

    this.text = spanned
}

fun TextView.setTextWithTagging(
    content: String?,
    mentions: MentionUIModel?,
    tags: List<TagUIModel>?,
    onUserMentionClick: (username: String, userId: Int) -> Unit,
    onHashTagClick: (hashTag: TagUIModel) -> Unit
) {
    val commentContent = content ?: ""
    this.text = commentContent

    val listMentions = (if (!mentions?.content.isNullOrEmpty()) {
        mentions?.content
    } else if (!mentions?.caption.isNullOrEmpty()) {
        mentions?.caption
    } else {
        listOf()
    }) ?: listOf()
    val listTags = tags ?: listOf()

    val color = ContextCompat.getColor(this.context, R.color.primaryYellowColor)
    val spannable = SpannableString(commentContent)

    for (mention in listMentions) {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = color
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                onUserMentionClick(mention.username, mention.userId)
            }
        }
        try {
            spannable.setSpan(clickableSpan, mention.startIndex, mention.endIndex, 0)
        } catch (throwable: Throwable) {
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace()
            }
        }
    }

    for (tag in listTags) {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = color
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                onHashTagClick(tag)
            }
        }
        try {
            // for now manually calculating the end index as the tag.endIndex is incorrect
            // the `tag.tag` is the actual tag text without '#', so we need +1
            val endIndex = tag.startIndex + tag.tag.length + 1
            spannable.setSpan(clickableSpan, tag.startIndex, endIndex, 0)
            spannable.setSpan(StyleSpan(Typeface.BOLD), tag.startIndex, endIndex, 0)
        } catch (throwable: Throwable) {
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace()
            }
        }
    }

    this.text = spannable
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setRightDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int) {
    try {
        var drawable: Drawable? = null
        if (id != 0) {
            drawable = ContextCompat.getDrawable(context, id)
            val size = resources.getDimensionPixelSize(sizeRes)
            drawable?.setBounds(0, 0, size, size)
        }
        this.setCompoundDrawables(null, null, drawable, null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}