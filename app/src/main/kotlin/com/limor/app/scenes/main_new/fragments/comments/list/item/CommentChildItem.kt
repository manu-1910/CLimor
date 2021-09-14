package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.ItemChildCommentBinding
import com.limor.app.databinding.ItemParentCommentBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

/**
 * @param isSimplified - should item be simplified (e.g. comment size limit)
 */
class CommentChildItem(
    val castOwnerId: Int,
    val parentComment: CommentUIModel,
    val comment: CommentUIModel,
    val isSimplified: Boolean,
    val onReplyClick: (parentComment: CommentUIModel, replyToComment: CommentUIModel) -> Unit,
    val onLikeClick: (comment: CommentUIModel, liked: Boolean) -> Unit,
    val onThreeDotsClick: (parentComment: CommentUIModel, item: CommentChildItem, position: Int) -> Unit,
    val onUserMentionClick: (username: String, userId: Int) -> Unit,
    val onCommentListen: (commentId: Int) -> Unit,
) : BindableItem<ItemChildCommentBinding>() {

    override fun bind(viewBinding: ItemChildCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.username
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getTimeAgoText(
                createdAt,
                viewBinding.root.context
            )
        }
        comment.user?.getAvatarUrl()?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.tvCastCreator.text = if (isOwnerOf(castOwnerId,comment)) "• Cast Creator" else ""

        val onUserClick: (view: View) -> Unit =  {
            onUserClick()
        }
        viewBinding.ivCommentAvatar.throttledClick(onClick = onUserClick)
        viewBinding.tvCommentName.throttledClick(onClick = onUserClick)

        setTextWithTagging(viewBinding.tvCommentContent)

        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(parentComment, comment)
        }
        viewBinding.btnCommentMore.setOnClickListener {
            onThreeDotsClick(comment, this, position)
        }
        if (isSimplified) {
            viewBinding.tvCommentContent.maxLines = 3
            viewBinding.tvCommentContent.ellipsize = TextUtils.TruncateAt.END
        } else {
            viewBinding.tvCommentContent.maxLines = Int.MAX_VALUE
        }
        initLikeState(viewBinding)
        setupListens(viewBinding)
        initAudioPlayer(viewBinding)
    }

    private fun onUserClick() {
        val user = comment.user ?: return
        val username = user.username ?: return
        onUserMentionClick(username, user.id)
    }

    private fun setTextWithTagging(tvCommentContent: TextView) {
        val commentContent = comment.content ?: ""
        tvCommentContent.text = commentContent

        if (comment.mentions == null) {
            return
        }

        val color = ContextCompat.getColor(tvCommentContent.context, R.color.primaryYellowColor)
        val spannable = SpannableString(commentContent)

        for (mention in comment.mentions.content) {
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
        tvCommentContent.text = spannable
        tvCommentContent.movementMethod = LinkMovementMethod.getInstance();
    }

    private fun initAudioPlayer(binding: ItemChildCommentBinding) {
        binding.audioPlayer.apply {
            if (comment.audio != null) {
                makeVisible()
                initialize(comment.audio)
            } else {
                makeGone()
            }
        }
    }

    private fun initLikeState(binding: ItemChildCommentBinding) {
        binding.apply {
            btnCommentLike.isLiked = comment.isLiked!!

            comment.likesCount?.let {
                if (it > 0) {
                    likesCount.visibility = View.VISIBLE
                } else {
                    likesCount.visibility = View.GONE
                }
            }

            likesCount.text = root.context.resources.getQuantityString(
                R.plurals.likes_count,
                comment.likesCount ?: 0,
                comment.likesCount ?: 0
            )

            btnCommentLike.setOnClickListener {
                val isLiked = btnCommentLike.isLiked
                val textLikesCount =
                    likesCount.text.toString().takeWhile { it.isDigit() || it == '-' }.toInt()
                val newLikesCount = if (isLiked) {
                    textLikesCount + 1
                } else {
                    textLikesCount - 1
                }

                if (newLikesCount > 0) {
                    likesCount.visibility = View.VISIBLE
                } else {
                    likesCount.visibility = View.GONE
                }


                likesCount.text = root.context.resources.getQuantityString(
                    R.plurals.likes_count,
                    newLikesCount,
                    newLikesCount
                )

                onLikeClick(comment, isLiked)
            }
        }
    }

    private fun setListensUI(binding: ItemChildCommentBinding, listensCountValue: Int) {

        binding.listensCount.tag = listensCountValue
        binding.listensCount.apply {
            visibility = if (listensCountValue > 0) View.VISIBLE else View.GONE
            text = binding.root.context.resources.getQuantityString(
                R.plurals.listens_count,
                listensCountValue,
                listensCountValue
            )
        }
    }

    private fun setupListens(binding: ItemChildCommentBinding) {
        setListensUI(binding, comment.listensCount ?: 0)

        binding.audioPlayer.playListener = {
            val newCount = binding.listensCount.tag as Int + 1
            binding.listensCount.tag = newCount
            setListensUI(binding, newCount)
            onCommentListen.invoke(comment.id)
        }
    }

    override fun getLayout() = R.layout.item_child_comment
    override fun initializeViewBinding(view: View) = ItemChildCommentBinding.bind(view)
    private fun isOwnerOf(id: Int, comment: CommentUIModel): Boolean {
        Timber.d("Owner Comment $id-> ${comment.user?.id}")
        return id == comment.user?.id
    }
}
