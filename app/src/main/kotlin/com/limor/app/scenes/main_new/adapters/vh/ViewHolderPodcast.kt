package com.limor.app.scenes.main_new.adapters.vh

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.android.billingclient.api.ProductDetails
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.MySpannable
import com.limor.app.scenes.patron.unipaas.UniPaasActivity
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.service.DetailsAvailableListener
import com.limor.app.service.ProductDetailsFetcher
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class ViewHolderPodcast(
    val binding: ItemHomeFeedBinding,
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel, sku: ProductDetails?) -> Unit,
    private val onRecastClick: (castId: Int, isRecasted: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel, ProductDetails?) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onReloadData: (castId: Int, reload: Boolean) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val onUserMentionClick: (username: String, userId: Int) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick: (cast: CastUIModel, play: Boolean) -> Unit,
    private val onEditPriceClick: (cast: CastUIModel) -> Unit,
    private val onPurchaseCast: (cast: CastUIModel, sku: ProductDetails?) -> Unit,
    private val productDetailsFetcher: ProductDetailsFetcher
) : ViewHolderBindable<DataItem>(binding), DetailsAvailableListener {

    private var playingPreview = false
    private var productDetails: ProductDetails? = null
    private var cast: CastUIModel? = null

    override fun bind(item: DataItem) {
        cast = item as CastUIModel

        setPodcastGeneralInfo(item)
        setPodcastOwnerInfo(item)
        setPodcastCounters(item)
        setPatronPodcastStatus(item)
        setAudioInfo(item)
        loadImages(item)
        setOnClicks(item)
        initLikeState(item)
        initRecastState(item)
        initShareState(item)
    }

    private fun setPodcastGeneralInfo(item: CastUIModel) {
        binding.tvPodcastLength.text = item.audio?.duration?.let { CommonsKt.getFeedDuration(it) }
        binding.tvPodcastTitle.text = SpannableString(item.title)
        binding.tvPodcastSubtitle.setTextWithTagging(
            item.caption,
            item.mentions,
            item.tags,
            onUserMentionClick,
            onHashTagClick
        )
        makeTextViewResizable(binding.tvPodcastTitle, 60, "..See More", true, item)
        makeTextViewResizable(binding.tvPodcastSubtitle, 60, "..See More", true, item) {
            binding.tvPodcastSubtitle.setTextWithTagging(
                item.caption,
                item.mentions,
                item.tags,
                onUserMentionClick,
                onHashTagClick
            )
        }
        if (item.patronCast == true) {
            binding.patronCastIndicator.visibility = View.VISIBLE
        } else {
            binding.patronCastIndicator.visibility = View.GONE
        }
        binding.matureContentInfo.visibility = if (item.maturedContent == true)
            View.VISIBLE
        else
            View.GONE
    }

    private fun setPodcastOwnerInfo(item: CastUIModel) {
        binding.tvPodcastUserName.text = item.owner?.username
        binding.tvPodcastUserSubtitle.text = item.getCreationDateAndPlace(context, true)
        binding.ivVerifiedAvatar.visibility =
            if (item.owner?.isVerified == true) View.VISIBLE else View.GONE
    }

    private fun setPodcastCounters(item: CastUIModel) {
        binding.tvPodcastLikes.text = item.likesCount?.toString()
        binding.tvPodcastRecast.text = item.recastsCount?.toString()
        binding.tvPodcastComments.text = item.commentsCount?.toString()
        binding.tvPodcastReply.text = item.sharesCount?.toString()
        binding.tvPodcastNumberOfListeners.text =
            if (item.listensCount == 0) "0" else item.listensCount?.toLong()?.formatHumanReadable
    }

    private fun hidePatronControls() {
        binding.apply {
            notCastOwnerActions.ensureGone()
            castOwnerActions.ensureGone()
            btnPurchasedCast.ensureGone()
            patronCastIndicator.ensureGone()
        }
    }

    private fun setPatronPodcastStatus(item: CastUIModel) {
        // Always hide patron controls regardless of the context, this ensures that they aren't
        // shown incorrectly when a view holder is recycled.
        hidePatronControls()

        if (item.patronCast == true) {
            val userId = PrefsHandler.getCurrentUserId(context)
            binding.patronCastIndicator.visibility = View.VISIBLE
            binding.btnBuyCast.setOnClickListener {
                productDetails?.let {
                    onPurchaseCast(item, it)
                }
            }

            when {
                (item.owner?.id != userId) -> {
                    if (item.patronDetails?.purchased == true) {
                        binding.btnPurchasedCast.visibility = View.VISIBLE
                        binding.notCastOwnerActions.visibility = View.GONE
                        binding.castOwnerActions.visibility = View.GONE
                        binding.btnPurchasedCast.text =
                            "Purchased at ${item.patronDetails?.castPurchasedDetails?.purchased_in_currency} ${item.patronDetails?.castPurchasedDetails?.purchased_at_price} "
                    } else {
                        //Purchase a cast actions
                        binding.notCastOwnerActions.visibility = View.VISIBLE
                        binding.castOwnerActions.visibility = View.GONE
                        binding.btnPurchasedCast.visibility = View.GONE
                        setPricingLabel()
                    }
                }
                item.owner.id == userId -> {
                    //Self Patron Cast
                    binding.btnPlayStopPreview.visibility = View.GONE
                    binding.btnBuyCast.visibility = View.GONE
                    binding.castOwnerActions.visibility = View.VISIBLE
                    binding.notCastOwnerActions.visibility = View.GONE
                    binding.btnPurchasedCast.visibility = View.GONE
                    setPricingLabel()
                }
                else -> {
                    binding.notCastOwnerActions.visibility = View.GONE
                    binding.castOwnerActions.visibility = View.GONE
                    binding.btnPurchasedCast.visibility = View.VISIBLE

                }
            }
        }
    }

    private fun setAudioInfo(item: CastUIModel) {
        binding.cpiPodcastListeningProgress.progress = itemViewType
    }

    private fun loadImages(item: CastUIModel) {
        item.owner?.getAvatarUrl()?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
            binding.ivAvatarImageListening.loadCircleImage(it)
        }

        item.imageLinks?.large?.let {
            binding.ivPodcastBackground.loadImage(it)
        }

        //Handling the color background for podcast
        CommonsKt.handleColorFeed(item, binding.colorFeedText, context)
    }

    private fun setOnClicks(item: CastUIModel) {

        binding.clItemPodcastFeed.setOnClickListener {
            onCastClick(item, productDetails)
        }

        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile(item)
        }
        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile(item)
        }

        binding.btnPodcastComments.throttledClick {
            onCommentsClick(item, productDetails)
        }

        binding.tvPodcastComments.throttledClick {
            onCommentsClick(item, productDetails)
        }

        binding.sharesLayout.throttledClick {
            onShareClick(item) { shareResult ->
                item.updateShares(shareResult)
                initShareState(item)
            }
        }

        binding.btnAddPreview.setOnClickListener {
            onEditPreviewClick(item)
        }

        binding.btnEditPrice.setOnClickListener {
            onEditPriceClick(item)
        }

        binding.btnPlayStopPreview.setOnClickListener {
            playingPreview = !playingPreview
            binding.btnPlayStopPreview.text = if (playingPreview) "Stop" else "Preview"
            onPlayPreviewClick(item, playingPreview)
            item.patronDetails?.previewDuration.let {
                if (it != null) {
                    Handler().postDelayed(Runnable {
                        playingPreview = !playingPreview
                        binding.btnPlayStopPreview.text = if (playingPreview) "Stop" else "Preview"
                    }, it.toLong())
                }
            }
        }

    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        context.startActivity(userProfileIntent)
    }

    private fun initLikeState(item: CastUIModel) {
        fun applyLikeStyle(isLiked: Boolean) {
            binding.tvPodcastLikes.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isLiked) R.color.textAccent else R.color.white
                )
            )
        }

        binding.apply {
            applyLikeStyle(item.isLiked!!)
            btnPodcastLikes.isLiked = item.isLiked

            likeLayout.setOnClickListener {
                btnPodcastLikes.isLiked = !btnPodcastLikes.isLiked
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text =
                    (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                onLikeClick(item.id, isLiked)
            }
        }
    }

    private fun initRecastState(item: CastUIModel) {
        fun applyRecastState(isRecasted: Boolean) {
            binding.tvPodcastRecast.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isRecasted) R.color.textAccent else R.color.white
                )
            )
        }
        binding.apply {
            applyRecastState(item.isRecasted!!)
            btnPodcastRecast.recasted = item.isRecasted

            recastLayout.setOnClickListener {
                val isRecasted = !btnPodcastRecast.recasted
                val recastCount = binding.tvPodcastRecast.text.toString().toInt()

                applyRecastState(isRecasted)
                binding.tvPodcastRecast.text =
                    (if (isRecasted) recastCount + 1 else recastCount - 1).toString()
                binding.btnPodcastRecast.recasted = isRecasted

                onRecastClick(item.id, isRecasted)
            }
        }
    }

    private fun applyRecastStyle(isRecasted: Boolean) {
        binding.tvPodcastRecast.setTextColor(
            if (isRecasted) ContextCompat.getColor(
                binding.root.context,
                R.color.textAccent
            ) else
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.white
                )
        )
    }

    private fun initShareState(item: CastUIModel) {
        binding.tvPodcastReply.text = item.sharesCount.toString()
        binding.btnPodcastReply.shared = item.isShared == true
        applySharedState(item.isShared == true)
    }

    private fun applySharedState(isShared: Boolean) {
        // new requirement as of October 28th, 2021
        // - the share button shouldn't have state
        binding.tvPodcastReply.setTextColor(
            ContextCompat.getColor(binding.root.context, R.color.white)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setPricingLabel() {
        val priceId = cast?.patronDetails?.priceId ?: return
        val details = productDetails
        if (details == null || details.productId != priceId) {
            productDetailsFetcher.getPrice(priceId, this)
            return
        }
        binding.btnBuyCast.text =
            "${getString(R.string.buy_cast)}\n${details.oneTimePurchaseOfferDetails?.formattedPrice}"
        binding.btnEditPrice.text =
            "${getString(R.string.edit_price)}\n${details.oneTimePurchaseOfferDetails?.formattedPrice}"
    }

    override fun onDetailsAvailable(details: Map<String, ProductDetails>) {
        val priceId = cast?.patronDetails?.priceId ?: return
        if (details.containsKey(priceId)) {
            productDetails = details[priceId]
            setPricingLabel()
        }
    }

    fun makeTextViewResizable(
        tv: TextView,
        maxCharacters: Int,
        expandText: String,
        viewMore: Boolean,
        item: CastUIModel,
        reset: (() -> Unit)? = null
    ) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            if (maxCharacters > 0 && tv.text.length > maxCharacters) {
                tv.text = tv.text.subSequence(0, 60)
                    .toString() + " " + expandText
                tv.setText(
                    addClickablePartTextViewResizable(
                        Html.fromHtml(tv.text.toString()), tv, Int.MAX_VALUE, expandText,
                        viewMore, item, reset
                    ), TextView.BufferType.SPANNABLE
                )
                tv.movementMethod = LinkMovementMethod.getInstance()
            }
        })
    }

    private fun addClickablePartTextViewResizable(
        strSpanned: Spanned, tv: TextView,
        maxLine: Int, spanableText: String, viewMore: Boolean,
        item: CastUIModel,
        reset: (() -> Unit)? = null
    ): SpannableStringBuilder {
        val str = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spanableText)) {
            ssb.setSpan(
                StyleSpan(Typeface.NORMAL),
                str.indexOf(spanableText),
                str.indexOf(spanableText) + spanableText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                AbsoluteSizeSpan(14, true),
                str.indexOf(spanableText),
                str.indexOf(spanableText) + spanableText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(object : MySpannable(false) {
                override fun onClick(widget: View) {
                    if (viewMore) {
                        tv.movementMethod = null
                        tv.maxLines = Int.MAX_VALUE
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        reset?.invoke()
                    } else {
                        tv.maxLines = 2
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        makeTextViewResizable(tv, 3, "..See More", true, item, reset)
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
        }
        return ssb
    }

}