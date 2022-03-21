package com.limor.app.scenes.main_new.adapters.vh

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.android.billingclient.api.SkuDetails
import com.limor.app.R
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.extensions.setTextWithTagging
import com.limor.app.extensions.throttledClick
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.FragmentRecastUsers
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.service.DetailsAvailableListener
import com.limor.app.service.ProductDetails
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel

class ViewHolderRecast(
    val binding: ItemHomeFeedRecastedBinding,
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel, sku: SkuDetails?) -> Unit,
    private val onRecastClick: (castId: Int, isRecasted: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel, SkuDetails?) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val onUserMentionClick: (username: String, userId: Int) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick: (cast: CastUIModel, play: Boolean) -> Unit,
    private val onEditPriceClick: (cast: CastUIModel) -> Unit,
    private val onPurchaseCast: (cast: CastUIModel, sku: SkuDetails?) -> Unit,
    private val productDetailsFetcher: ProductDetails
) : ViewHolderBindable<DataItem>(binding), DetailsAvailableListener {

    private var playingPreview = false
    private var skuDetails: SkuDetails? = null
    private var cast: CastUIModel? = null

    override fun bind(item: DataItem) {
        cast = item as CastUIModel

        binding.tvRecastUserName.text = getCastersDescriptionText(item)
        binding.tvRecastUserSubtitle.text = item.getCreationDateAndPlace(context, false)

        binding.tvRecastMessage.text = ""

        binding.tvRecastPlayCurrentPosition.text = "???"
        binding.tvRecastPlayMaxPosition.text = "???"

        binding.tvPodcastUserName.text = item.owner?.username
        binding.ivPodcastUserVerifiedAvatar.visibility =
            if (item.owner?.isVerified == true) View.VISIBLE else View.GONE

        binding.tvPodcastUserSubtitle.text = item.getCreationDateAndPlace(context, true)

        binding.tvPodcastLength.text = item.audio?.duration?.let {
            "${it.toMinutes()}m ${it.minusMinutes(it.toMinutes()).seconds}s"
        }
        binding.tvPodcastTitle.text = item.title

        binding.tvPodcastSubtitle.setTextWithTagging(
            item.caption,
            item.mentions,
            item.tags,
            onUserMentionClick,
            onHashTagClick
        )

        if(item.recasted == true){
            binding.patronCastIndicator.visibility = View.VISIBLE
        }

        item.owner?.getAvatarUrl()?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
        }

        item.recaster?.getAvatarUrl()?.let {
            binding.ivRecastAvatar.loadCircleImage(it)
        }

        item.imageLinks?.large?.let {
            binding.ivPodcastBackground.loadImage(it)
        }

        //Handling the color background for podcast
        CommonsKt.handleColorFeed(item, binding.colorFeedText, context)

        setPodcastCounters(item)
        setInterationStatus(item)
        initLikeState(item)
        initRecastState(item)

        binding.btnRecastMore.setOnClickListener {
            val bundle = bundleOf(DialogPodcastMoreActions.CAST_KEY to item)

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }

        binding.btnPodcastMore.setOnClickListener {
            val bundle = bundleOf(DialogPodcastMoreActions.CAST_KEY to item)

            it.findNavController()
                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }

        binding.ivRecastAvatar.setOnClickListener {
            openRecasterProfile(item)
        }
        binding.tvRecastUserName.setOnClickListener {
            openRecasterProfile(item)
        }

        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile(item)
        }
        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile(item)
        }

        binding.castCard.setOnClickListener {
            onCastClicked(item)
        }

        binding.btnAddPreview.setOnClickListener {
            onEditPreviewClick(item)
        }

        binding.btnEditPrice.setOnClickListener {
            onEditPriceClick(item)
        }

        binding.tvRecastUserName.setOnClickListener {
            it?.findNavController()?.navigate(R.id.action_navigate_home_to_fragment_recasted_users, bundleOf(FragmentRecastUsers.PODCAST_ID_KEY to item.id))
        }

        if (item.patronCast == true) {
            val userId = PrefsHandler.getCurrentUserId(context)
            binding.btnBuyCast.setOnClickListener {
                skuDetails?.let {
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
        } else{
            binding.notCastOwnerActions.visibility = View.GONE
            binding.castOwnerActions.visibility = View.GONE
            binding.btnPurchasedCast.visibility = View.GONE
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

        binding.btnPodcastComments.throttledClick {
            onCommentsClick(item, null)
        }

        binding.btnPodcastReply.throttledClick {
            onShareClick(item) { shareResult ->
                item.updateShares(shareResult)
                binding.btnPodcastReply.shared = item.isShared ?: false
            }
        }

        skuDetails = null

        if (item.patronCast == true) {
            fetchDetails()
        }
    }

    private fun getCastersDescriptionText(item: CastUIModel): SpannableString {
        val username = item.recaster?.username ?: ""
        val text = when(item.recastsCount){
            1 -> "recasted this cast"
            2 -> "and 1 other recasted this cast"
            else -> "and ${item.recastsCount?.minus(1)} others recasted this cast"
        }
        var spannableString = SpannableString(username + (if(item.recaster?.isVerified == true) "" else " ") + text)
        spannableString.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, username.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(AbsoluteSizeSpan(16, true), 0, username.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.black)), 0, username.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(AbsoluteSizeSpan(14, true), username.length, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if(item.recaster?.isVerified == true){
            val start = username.length
            val end = username.length + 1
            val flag = 0
            val drawable = context.resources.getDrawable(R.drawable.ic_verified_badge)
            drawable.setBounds(4,0,drawable.intrinsicWidth,drawable.intrinsicHeight)
            spannableString.setSpan(ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), start, end, flag)
            SpannableStringBuilder()
                .append(spannableString.subSequence(0, username.length + 1))
                .append(" ")
                .append(spannableString.subSequence(username.length, spannableString.length))
                .append(" ")
                .also { spannableString = SpannableString.valueOf(it) }
        }else{
            SpannableStringBuilder()
                .append(spannableString.subSequence(0, username.length + 1))
                .append(" ")
                .append(spannableString.subSequence(username.length, spannableString.length))
                .append(" ")
                .also { spannableString = SpannableString.valueOf(it) }
        }
        return spannableString
    }

    @SuppressLint("SetTextI18n")
    private fun setPricingLabel() {
        val priceId = cast?.patronDetails?.priceId ?: return
        val details = skuDetails
        if (details == null || details.sku != priceId) {
            productDetailsFetcher.getPrice(priceId, this)
            return
        }
        binding.btnBuyCast.text = "${getString(R.string.buy_cast)}\n${details.price}"
        binding.btnEditPrice.text = "${getString(R.string.edit_price)}\n${details.price}"
    }

    private fun setPodcastCounters(item: CastUIModel) {
        binding.tvPodcastLikes.text = item.likesCount?.toString()
        binding.tvPodcastRecast.text = item.recastsCount?.toString()
        binding.tvPodcastComments.text = item.commentsCount?.toString()
        binding.tvPodcastReply.text = item.sharesCount?.toString() ?: "0"
    }

    private fun initLikeState(item: CastUIModel) {
        fun applyLikeStyle(isLiked: Boolean) {
            binding.tvPodcastLikes.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isLiked) R.color.textAccent else R.color.subtitle_text_color
                )
            )
        }

        binding.apply {
            applyLikeStyle(item.isLiked!!)
            btnPodcastLikes.isLiked = item.isLiked

            btnPodcastLikes.setOnClickListener {
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
                    if (isRecasted) R.color.textAccent else R.color.subtitle_text_color
                )
            )
        }
        binding.apply {
            applyRecastState(item.isRecasted!!)
            btnPodcastRecast.recasted = item.isRecasted

            btnPodcastRecast.setOnClickListener {
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

    private fun setInterationStatus(item: CastUIModel) {
        binding.btnPodcastLikes.isLiked = item.isLiked ?: false
        binding.tvPodcastLikes.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                if (item.isLiked == true) R.color.textAccent else R.color.subtitle_text_color
            )
        )
        binding.btnPodcastRecast.recasted = item.isRecasted ?: false
        binding.tvPodcastRecast.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                if (item.isRecasted == true) R.color.textAccent else R.color.subtitle_text_color
            )
        )
        binding.btnPodcastReply.shared = item.isShared ?: false
    }

    private fun fetchDetails() {
        val priceId = cast?.patronDetails?.priceId ?: return
        val details = skuDetails
        if (details == null || details.sku != priceId) {
            productDetailsFetcher.getPrice(priceId, this)
            return
        }
    }

    private fun onCastClicked(item: CastUIModel) {
        onCastClick(item, skuDetails)
        // (binding.root.context.getActivity() as? PlayerViewManager)?.showExtendedPlayer(item.id)
    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        context.startActivity(userProfileIntent)
    }

    private fun openRecasterProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.recaster?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.recaster?.id)
        context.startActivity(userProfileIntent)
    }

    override fun onDetailsAvailable(details: Map<String, SkuDetails>) {
        val priceId = cast?.patronDetails?.priceId ?: return
        if (details.containsKey(priceId)) {
            skuDetails = details[priceId]
            setPricingLabel()
        }
    }

}