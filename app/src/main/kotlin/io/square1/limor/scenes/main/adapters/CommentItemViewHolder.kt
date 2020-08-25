package io.square1.limor.scenes.main.adapters

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.square1.limor.R
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIComment
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommentItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val commentClickListener: CommentsAdapter.OnCommentClickListener
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_comment_item_recycler_view,
        parent,
        false
    )
) {
    var tvUserFullname: TextView = itemView.findViewById(R.id.tvUserName)
    var tvDateAndLocation: TextView = itemView.findViewById(R.id.tvTimeAndLocation)

    var tvListens: TextView = itemView.findViewById(R.id.tvListens)
    var tvComments: TextView = itemView.findViewById(R.id.tvComments)
    var tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
    var tvRecasts: TextView = itemView.findViewById(R.id.tvRecasts)
    var tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)

    var ivUser: ImageView = itemView.findViewById(R.id.ivUserPicture)
    var ivVerifiedUser: ImageView = itemView.findViewById(R.id.ivVerifiedUser)

    var ibtnListen: ImageButton = itemView.findViewById(R.id.btnListens)
    var ibtnLike: ImageButton = itemView.findViewById(R.id.btnLikes)
    var ibtnRecasts: ImageButton = itemView.findViewById(R.id.btnRecasts)
    var ibtnComments: ImageButton = itemView.findViewById(R.id.btnComments)
    var ibtnMore: ImageButton = itemView.findViewById(R.id.btnMore)

    var btnReply: TextView = itemView.findViewById(R.id.btnReply)



    var clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(textView: View) {
            val tv = textView as TextView
            val s: Spanned = tv.text as Spanned
            val start: Int = s.getSpanStart(this)
            val end: Int = s.getSpanEnd(this)
            val clickedTag = s.subSequence(start, end).toString()
            commentClickListener.onHashtagClicked(clickedTag)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = true
        }
    }


    fun bind(currentItem: UIComment, position: Int) {
        // fullname
        val fullname: String = currentItem.user?.first_name + " " + currentItem.user?.last_name
        tvUserFullname.text = fullname
        tvUserFullname.onClick { commentClickListener.onUserClicked(currentItem, position) }


        // datetime and location
        val datetimeString =
            CommonsKt.getDateTimeFormattedFromTimestamp(currentItem.created_at.toLong())
        tvDateAndLocation.text = datetimeString


        // comment text
        tvCommentText.text = hightlightHashtags(currentItem.content)


        // recasts
        currentItem.podcast?.number_of_recasts?.let { tvRecasts.text = it.toString() }
        tvRecasts.onClick { commentClickListener.onRecastClicked(currentItem, position) }
        ibtnRecasts.onClick { commentClickListener.onRecastClicked(currentItem, position) }

        // likes
        currentItem.number_of_likes.let { tvLikes.text = it.toString() }
        tvLikes.onClick {
            commentClickListener.onLikeClicked(currentItem, position)
        }
        ibtnLike.onClick {
            commentClickListener.onLikeClicked(currentItem, position)
        }

        // listens
        currentItem.number_of_listens.let { tvListens.text = it.toString() }
        tvListens.onClick { commentClickListener.onListenClicked(currentItem, position) }
        ibtnListen.onClick { commentClickListener.onListenClicked(currentItem, position) }

        // comments
        currentItem.comment_count.let { tvComments.text = it.toString() }
        tvComments.onClick { commentClickListener.onCommentClicked(currentItem, position) }
        ibtnComments.onClick { commentClickListener.onCommentClicked(currentItem, position) }

        // user picture
        Glide.with(itemView.context)
            .load(currentItem.user?.images?.small_url)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)
        ivUser.onClick { commentClickListener.onUserClicked(currentItem, position) }

        // verified
        currentItem.user?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
            else
                ivVerifiedUser.visibility = View.GONE
        } ?: run { ivVerifiedUser.visibility = View.GONE }


        // like
        currentItem.liked.let {
            if (it)
                ibtnLike.setImageResource(R.drawable.like_filled)
            else
                ibtnLike.setImageResource(R.drawable.like)
        }


        ibtnMore.onClick { commentClickListener.onMoreClicked(currentItem, position) }
        btnReply.onClick { commentClickListener.onReplyClicked(currentItem, position) }
    }


    private fun hightlightHashtags(caption: String?): SpannableString? {
        caption?.let {
            val hashtaggedString = SpannableString(caption)
            val regex = "#[\\w]+"

            val pattern: Pattern = Pattern.compile(regex, Pattern.MULTILINE)
            val matcher: Matcher = pattern.matcher(caption)

            while (matcher.find()) {
                val textFound = matcher.group(0)
                val startIndex = matcher.start(0)
                val endIndex = matcher.end(0)
                hashtaggedString.setSpan(clickableSpan, startIndex, endIndex, 0)
                println("Hemos encontrado el texto $textFound que empieza en $startIndex y acaba en $endIndex")
            }
            return hashtaggedString
        }
        return SpannableString("")
    }

}