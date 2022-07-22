package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.ItemChildCommentBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.DateUiUtil
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
    var isSimplified: Boolean,
    val onReplyClick: (parentComment: CommentUIModel, replyToComment: CommentUIModel) -> Unit,
    val onLikeClick: (comment: CommentUIModel, liked: Boolean) -> Unit,
    val onThreeDotsClick: (parentComment: CommentUIModel, item: CommentChildItem, position: Int) -> Unit,
    val onUserMentionClick: (username: String, userId: Int) -> Unit,
    val onCommentListen: (commentId: Int) -> Unit,
    val highlight: Boolean
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
        viewBinding.tvCastCreator.text =
            if (isOwnerOf(castOwnerId, comment)) "• Cast Creator" else ""

        val onUserClick: (view: View) -> Unit = {
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
            viewBinding.tvCommentContent.maxLines = 2
            viewBinding.tvCommentContent.ellipsize = TextUtils.TruncateAt.END
        } else {
            viewBinding.tvCommentContent.maxLines = Int.MAX_VALUE
        }
        initLikeState(viewBinding)
        setupListens(viewBinding)
        initAudioPlayer(viewBinding)
        if (highlight) {
            blinkBackground(viewBinding)
        }
    }

    private fun blinkBackground(binding: ItemChildCommentBinding) {
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
            println("child.comment.links -> ${comment.links}")
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
        tvCommentContent.movementMethod = LinkMovementMethod.getInstance()
        val maxLines = if (isSimplified) 2 else Int.MAX_VALUE
        if (isSimplified)
            makeTextViewResizable(tvCommentContent, maxLines, "..See More", true)
        else {
            tvCommentContent.text = spannable
        }
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

    fun makeTextViewResizable(tv: TextView, maxLine: Int, expandText: String, viewMore: Boolean) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            if (maxLine == 0) {
                val lineEndIndex = tv.layout.getLineEnd(0)
                val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                    .toString() + " " + expandText
                tv.text = text
                tv.setText(
                    addClickablePartTextViewResizable(
                        Html.fromHtml(tv.text.toString()), tv, Int.MAX_VALUE, expandText,
                        viewMore
                    ), TextView.BufferType.SPANNABLE
                )
            } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                val lineEndIndex = tv.layout.getLineEnd(maxLine - 1) - 10
                val lastIndex = lineEndIndex - expandText.length + 1
                val text = if (lastIndex > 0) tv.text.subSequence(0, lastIndex)
                    .toString() + " " + expandText else tv.text.subSequence(0, lineEndIndex)
                    .toString() + " " + expandText
                tv.text = text
                tv.setText(
                    addClickablePartTextViewResizable(
                        Html.fromHtml(tv.text.toString()), tv, Int.MAX_VALUE, expandText,
                        viewMore
                    ), TextView.BufferType.SPANNABLE
                )
            } else {
                tv.setText(
                    addClickablePartTextViewResizable(
                        Html.fromHtml(tv.text.toString()), tv, maxLine, expandText,
                        viewMore
                    ), TextView.BufferType.SPANNABLE
                )
            }
        })
    }

    private fun addClickablePartTextViewResizable(
        strSpanned: Spanned, tv: TextView,
        maxLine: Int, spanableText: String, viewMore: Boolean
    ): SpannableStringBuilder? {
        val str = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spanableText)) {
            ssb.setSpan(object : MySpannable(false) {
                override fun onClick(widget: View) {
                    if (viewMore) {
                        tv.movementMethod = null
                        tv.maxLines = Int.MAX_VALUE
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        isSimplified = false
                        //makeTextViewResizable(tv, Int.MAX_VALUE, "See Less", false)
                        setTextWithTagging(tv)
                    } else {
                        tv.maxLines = 2
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        makeTextViewResizable(tv, 3, "..See More", true)
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
        }
        return ssb
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

open class MySpannable(isUnderline: Boolean) : ClickableSpan() {
    private var isUnderline = true
    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = isUnderline
        ds.color = Color.parseColor("#ED9D0C")
    }

    override fun onClick(widget: View) {}

    init {
        this.isUnderline = isUnderline
    }
}
