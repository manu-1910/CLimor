package io.square1.limor.scenes.main.adapters

import android.content.Context
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
import io.square1.limor.common.Constants.Companion.MAX_API_COMMENTS_PER_COMMENT
import io.square1.limor.scenes.main.fragments.podcast.CommentWithParent
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIPodcast
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
    private var tvUserFullname: TextView = itemView.findViewById(R.id.tvUserName)
    private var tvDateAndLocation: TextView = itemView.findViewById(R.id.tvTimeAndLocation)

    private var tvListens: TextView = itemView.findViewById(R.id.tvListens)
    private var tvComments: TextView = itemView.findViewById(R.id.tvComments)
    private var tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
    private var tvRecasts: TextView = itemView.findViewById(R.id.tvRecasts)
    private var tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
    private var tvNameReplyingTo: TextView = itemView.findViewById(R.id.tvNameReplyingTo)
    private var tvMoreReplies: TextView = itemView.findViewById(R.id.tvMoreReplies)

    private var ivUser: ImageView = itemView.findViewById(R.id.ivUserPicture)
    private var ivVerifiedUser: ImageView = itemView.findViewById(R.id.ivVerifiedUser)

    private var ibtnListen: ImageButton = itemView.findViewById(R.id.btnListens)
    private var ibtnLike: ImageButton = itemView.findViewById(R.id.btnLikes)
    private var ibtnRecasts: ImageButton = itemView.findViewById(R.id.btnRecasts)
    private var ibtnComments: ImageButton = itemView.findViewById(R.id.btnComments)
    private var ibtnMore: ImageButton = itemView.findViewById(R.id.btnMore)

    private var btnReply: TextView = itemView.findViewById(R.id.btnReply)
    private var barThreadUp: View = itemView.findViewById(R.id.barThreadUp)
    private var barThreadDown: View = itemView.findViewById(R.id.barThreadDown)
    private var barDecorator: View = itemView.findViewById(R.id.barDecorator)
    private var layReplyingTo: View = itemView.findViewById(R.id.layReplying)
    private var layMoreReplies: View = itemView.findViewById(R.id.layMoreReplies)


    private var clickableSpan: ClickableSpan = object : ClickableSpan() {
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



    fun bind(
        currentItem: CommentWithParent,
        podcastParent: UIPodcast,
        context: Context,
        position: Int
    ) {
        // fullname
        val fullname: String = currentItem.comment.user?.first_name + " " + currentItem.comment.user?.last_name
        tvUserFullname.text = fullname
        tvUserFullname.onClick { commentClickListener.onUserClicked(currentItem.comment, position) }


        // replying to
        // if the parent is null, it means that we are a comment and that our parent is the main podcast
        if(currentItem.parent == null) {
            // we have to write the name who we reply to
            val fullnameReply: String = " " + podcastParent.user.first_name + " " + podcastParent.user.last_name
            tvNameReplyingTo.text = fullnameReply
            layReplyingTo.visibility = View.VISIBLE
            layMoreReplies.visibility = View.GONE

            // remove the upper bar
            barThreadUp.visibility = View.GONE

            // if we have children we'll draw the lower bar
            if(currentItem.comment.comment_count > 0) {
                barThreadDown.visibility = View.VISIBLE
                barDecorator.visibility = View.GONE
            } else {
                barThreadDown.visibility = View.GONE
                barDecorator.visibility = View.VISIBLE
            }

            // if the parent is not null, this means that we are a comment of a comment and that our parent is a comment
        } else {
            barThreadUp.visibility = View.VISIBLE
            layReplyingTo.visibility = View.GONE

            // if we are NOT the last comment of our parent, then we have to draw the lower bar and remove the bar decorator
            if(currentItem.parent.comment.comments.last() != currentItem.comment) {
                barThreadDown.visibility = View.VISIBLE
                barDecorator.visibility = View.GONE
                layMoreReplies.visibility = View.GONE

                // if we ARE the last comment of our parent, then we have to remove the lower bar and draw the bar decorator
            } else {
                barThreadDown.visibility = View.GONE
                barDecorator.visibility = View.VISIBLE

                // if we are the last child and our parent has more comments than the number of comments loaded, we have to show the more replies layout
                // and
                if(currentItem.parent.comment.comment_count > currentItem.parent.comment.comments.size) {
                    layMoreReplies.visibility = View.VISIBLE
                    val numberOfCommentsMore = currentItem.parent.comment.comment_count - MAX_API_COMMENTS_PER_COMMENT
                    val moreRepliesText = numberOfCommentsMore.toString() + " " + context.getString(R.string.more_replies)
                    tvMoreReplies.text = moreRepliesText
                    tvMoreReplies.onClick { commentClickListener.onMoreRepliesClicked(currentItem.parent, position) }

                    // this means that we have to show the show less button. This means that we are showing all the comments of the parent and that the parent
                    // has more than the default api comment count
                } else if(currentItem.parent.comment.comment_count == currentItem.parent.comment.comments.size && currentItem.parent.comment.comment_count > MAX_API_COMMENTS_PER_COMMENT) {
                    layMoreReplies.visibility = View.VISIBLE
                    tvMoreReplies.text = context.getString(R.string.show_less)
                    tvMoreReplies.onClick { commentClickListener.onShowLessClicked(currentItem.parent, position) }

                } else {
                    layMoreReplies.visibility = View.GONE
                }
            }
        }


        // datetime and location
        val datetimeString =
            CommonsKt.getDateTimeFormattedFromTimestamp(currentItem.comment.created_at.toLong())
        tvDateAndLocation.text = datetimeString


        // comment text
        tvCommentText.text = hightlightHashtags(currentItem.comment.content)
        tvCommentText.onClick { commentClickListener.onItemClicked(currentItem, position) }


        // recasts
//        currentItem.comment.podcast?.number_of_recasts?.let { tvRecasts.text = it.toString() }
        tvRecasts.onClick { commentClickListener.onRecastClicked(currentItem.comment, position) }
        ibtnRecasts.onClick { commentClickListener.onRecastClicked(currentItem.comment, position) }

        // likes
        currentItem.comment.number_of_likes.let { tvLikes.text = it.toString() }
        tvLikes.onClick {
            commentClickListener.onLikeClicked(currentItem.comment, position)
        }
        ibtnLike.onClick {
            commentClickListener.onLikeClicked(currentItem.comment, position)
        }

        // listens
        currentItem.comment.number_of_listens.let { tvListens.text = it.toString() }
        tvListens.onClick { commentClickListener.onListenClicked(currentItem.comment, position) }
        ibtnListen.onClick { commentClickListener.onListenClicked(currentItem.comment, position) }

        // comments
        currentItem.comment.comment_count.let { tvComments.text = it.toString() }
        tvComments.onClick { commentClickListener.onCommentClicked(currentItem.comment, position) }
        ibtnComments.onClick { commentClickListener.onCommentClicked(currentItem.comment, position) }

        // user picture
        Glide.with(itemView.context)
            .load(currentItem.comment.user?.images?.small_url)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)
        ivUser.onClick { commentClickListener.onUserClicked(currentItem.comment, position) }

        // verified
        currentItem.comment.user?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
            else
                ivVerifiedUser.visibility = View.GONE
        } ?: run { ivVerifiedUser.visibility = View.GONE }


        // like
        currentItem.comment.liked.let {
            if (it)
                ibtnLike.setImageResource(R.drawable.like_filled)
            else
                ibtnLike.setImageResource(R.drawable.like)
        }


        ibtnMore.onClick { commentClickListener.onMoreClicked(currentItem.comment, position) }
        btnReply.onClick { commentClickListener.onReplyClicked(currentItem.comment, position) }
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