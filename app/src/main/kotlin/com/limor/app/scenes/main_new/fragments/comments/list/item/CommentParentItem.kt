package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.ItemChildCommentBinding
import com.limor.app.databinding.ItemParentCommentBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

class CommentParentItem(
    val castOwnerId: Int,
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel) -> Unit,
    val onThreeDotsClick: (parentComment: CommentUIModel, item: CommentParentItem) -> Unit,
    val onLikeClick: (parentComment: CommentUIModel, liked: Boolean) -> Unit,
    val onUserMentionClick: (username: String, userId: Int) -> Unit,
    val onCommentListen: (commentId: Int) -> Unit,
    val highlight: Boolean
) : BindableItem<ItemParentCommentBinding>() {

    override fun bind(viewBinding: ItemParentCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.username
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getTimeAgoText(
                createdAt,
                viewBinding.root.context
            )
        }

        setTextWithTagging(viewBinding.tvCommentContent)

        val onUserClick: (view: View) -> Unit = {
            onUserClick()
        }
        viewBinding.ivCommentAvatar.throttledClick(onClick = onUserClick)
        viewBinding.tvCommentName.throttledClick(onClick = onUserClick)

        comment.user?.getAvatarUrl()?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.tvCastCreator.text =
            if (isOwnerOf(castOwnerId, comment)) "• Cast Creator" else ""

        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(comment)
        }
        viewBinding.btnCommentMore.setOnClickListener {
            onThreeDotsClick(comment, this)
        }
        initLikeState(viewBinding)
        initAudioPlayer(viewBinding)
        if (highlight) {
            blinkBackground(viewBinding)
        }
    }

    private fun blinkBackground(binding: ItemParentCommentBinding) {
        val anim: ObjectAnimator = ObjectAnimator.ofInt(
            binding.mainLayout,
            "backgroundColor",
            ContextCompat.getColor(binding.mainLayout.context, R.color.un_read_background),
            Color.WHITE
        )
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                setTextWithTagging(binding.tvCommentContent)
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })
        anim.duration = 2000
        anim.setEvaluator(ArgbEvaluator())
        anim.repeatCount = 0
        anim.start()
    }

    private fun onUserClick() {
        val user = comment.user ?: return
        val username = user.username ?: return
        onUserMentionClick(username, user.id)
    }

    private fun setTextWithTagging(tvCommentContent: TextView) {
        val commentContent = comment.content ?: ""
        tvCommentContent.text = commentContent

        val mentions = comment.mentions?.content ?: listOf()
        val links = comment.links?.content ?: listOf()

        if (BuildConfig.DEBUG) {
            println("parent.comment.links -> ${comment.links}")
        }

        val color = ContextCompat.getColor(tvCommentContent.context, R.color.primaryYellowColor)
        val spannable = SpannableString(commentContent)

        for (mention in mentions) {
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

        for (link in links) {
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = color
                    ds.isUnderlineText = true
                }

                override fun onClick(widget: View) {
                    if (BuildConfig.DEBUG) {
                        print("Link is -> $link")
                    }
                    CommonsKt.openUrlInBrowser(widget.context, link.link)
                }
            }
            try {
                spannable.setSpan(
                    clickableSpan,
                    link.startIndex,
                    link.endIndex + 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            } catch (throwable: Throwable) {
                if (BuildConfig.DEBUG) {
                    throwable.printStackTrace()
                }
            }
        }

        tvCommentContent.text = spannable
        tvCommentContent.movementMethod = LinkMovementMethod.getInstance();
    }

    private fun initAudioPlayer(binding: ItemParentCommentBinding) {
        binding.audioPlayer.apply {
            if (comment.audio != null) {
                makeVisible()
                initialize(comment.audio)
            } else {
                makeGone()
            }
        }
    }

    private fun initLikeState(binding: ItemParentCommentBinding) {
        setupListens(binding)

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

            likeCommentLayout.setOnClickListener {
                btnCommentLike.isLiked = !btnCommentLike.isLiked
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

    private fun setListensUI(binding: ItemParentCommentBinding, listensCountValue: Int) {

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

    private fun setupListens(binding: ItemParentCommentBinding) {
        setListensUI(binding, comment.listensCount ?: 0)

        binding.audioPlayer.playListener = {
            val newCount = binding.listensCount.tag as Int + 1
            binding.listensCount.tag = newCount
            setListensUI(binding, newCount)
            onCommentListen.invoke(comment.id)
        }
    }

    override fun getLayout() = R.layout.item_parent_comment
    override fun initializeViewBinding(view: View) = ItemParentCommentBinding.bind(view)

    private fun isOwnerOf(id: Int, comment: CommentUIModel): Boolean {
        Timber.d("Owner Comment $id-> ${comment.user?.id}")
        return id == comment.user?.id
    }
}
