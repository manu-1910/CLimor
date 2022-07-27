package com.limor.app.scenes.main_new.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.limor.app.R
import com.limor.app.databinding.FragmentPodcastPopupBinding
import com.limor.app.di.Injectable
import com.limor.app.dm.ShareResult
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.fragments.comments.list.item.MySpannable
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.DetailsAvailableListener
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PurchaseTarget
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.mapToAudioTrack
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import kotlinx.coroutines.launch
import javax.inject.Inject

class FragmentPodcastPopup : DialogFragment(), Injectable, DetailsAvailableListener {

    companion object {
        val TAG = FragmentPodcastPopup::class.qualifiedName
        const val CAST_KEY = "CAST_KEY"
        const val PARENT_COMMENT_ID_KEY = "PARENT_COMMENT_ID_KEY"
        const val CHILD_COMMENT_ID_KEY = "CHILD_COMMENT_ID_KEY"

        fun newInstance(castId: Int, parentCommentId: Int = -1, childCommentId: Int = -1
        ): FragmentPodcastPopup {
            return FragmentPodcastPopup().apply {
                arguments = bundleOf(CAST_KEY to castId, PARENT_COMMENT_ID_KEY to parentCommentId, CHILD_COMMENT_ID_KEY to childCommentId)
            }
        }
    }

    @Inject
    lateinit var playBillingHandler: PlayBillingHandler

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val podcastViewModel : PodcastViewModel by viewModels{viewModelFactory}
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }

    private val podcastId: Int by lazy {
        requireArguments().getInt(
            FragmentPodcastPopup.CAST_KEY,
            -1
        )
    }

    private val parentCommentId: Int by lazy {
        requireArguments().getInt(
            FragmentPodcastPopup.PARENT_COMMENT_ID_KEY,
            -1
        )
    }

    private val childCommentId: Int by lazy {
        requireArguments().getInt(
            FragmentPodcastPopup.CHILD_COMMENT_ID_KEY,
            -1
        )
    }

    private lateinit var binding: FragmentPodcastPopupBinding

    private var playingPreview = false
    private var productDetails: ProductDetails? = null

    private lateinit var cast: CastUIModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_FloatingDialog)
        podcastViewModel.loadCast(podcastId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPodcastPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        podcastViewModel.cast.observe(viewLifecycleOwner, {
            cast = it
            setPodcastGeneralInfo()
            setPodcastOwnerInfo()
            setPodcastCounters()
            setPatronPodcastStatus()
            fetchDetails()
            setAudioInfo()
            loadImages()
            setOnClicks()
            initLikeState()
            initRecastState()
            initShareState()
        })
    }

    private fun setPodcastGeneralInfo() {
        binding.tvPodcastLength.text = cast.audio?.duration?.let { CommonsKt.getFeedDuration(it) }
        binding.tvPodcastTitle.text = SpannableString(cast.title)
        binding.tvPodcastSubtitle.setTextWithTagging(
            cast.caption,
            cast.mentions,
            cast.tags,
            onUserMentionClick,
            onHashTagClick
        )
        makeTextViewResizable(binding.tvPodcastSubtitle, 60, "..See More", true, cast) {
            binding.tvPodcastSubtitle.setTextWithTagging(
                cast.caption,
                cast.mentions,
                cast.tags,
                onUserMentionClick,
                onHashTagClick
            )
        }
        binding.matureContentInfo.visibility = if (cast.maturedContent == true)
            View.VISIBLE
        else
            View.GONE
    }

    private fun setPodcastOwnerInfo() {
        binding.tvPodcastUserName.text = cast.owner?.username
        binding.tvPodcastUserSubtitle.text = cast.getCreationDateAndPlace(requireContext(), true)
        binding.ivVerifiedAvatar.visibility =
            if (cast.owner?.isVerified == true) View.VISIBLE else View.GONE
    }

    private fun setPodcastCounters() {
        binding.tvPodcastLikes.text = cast.likesCount?.toString()
        binding.tvPodcastRecast.text = cast.recastsCount?.toString()
        binding.tvPodcastComments.text = cast.commentsCount?.toString()
        binding.tvPodcastReply.text = cast.sharesCount?.toString()
        binding.tvPodcastNumberOfListeners.text =
            if (cast.listensCount == 0) "0" else cast.listensCount?.toLong()?.formatHumanReadable
    }

    private fun hidePatronControls() {
        binding.apply {
            notCastOwnerActions.ensureGone()
            castOwnerActions.ensureGone()
            btnPurchasedCast.ensureGone()
        }
    }

    private fun setPatronPodcastStatus() {
        // Always hide patron controls regardless of the context, this ensures that they aren't
        // shown incorrectly when a view holder is recycled.
        hidePatronControls()

        if (cast.patronCast == true) {
            val userId = PrefsHandler.getCurrentUserId(requireContext())
            binding.btnBuyCast.setOnClickListener {
                productDetails?.let {
                    launchPurchaseCast(cast, productDetails)
                }
            }

            when {
                (cast.owner?.id != userId) -> {
                    if (cast.patronDetails?.purchased == true) {
                        binding.btnPurchasedCast.visibility = View.VISIBLE
                        binding.notCastOwnerActions.visibility = View.GONE
                        binding.castOwnerActions.visibility = View.GONE
                        binding.btnPurchasedCast.text =
                            "Purchased at ${cast.patronDetails?.castPurchasedDetails?.purchased_in_currency} ${cast.patronDetails?.castPurchasedDetails?.purchased_at_price} "
                    } else {
                        //Purchase a cast actions
                        binding.notCastOwnerActions.visibility = View.VISIBLE
                        binding.castOwnerActions.visibility = View.GONE
                        binding.btnPurchasedCast.visibility = View.GONE
                        setPricingLabel()
                    }
                }
                (cast.owner!!.id == userId) -> {
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

    private fun setAudioInfo() {
        binding.cpiPodcastListeningProgress.progress = -1
    }

    private fun loadImages() {
        cast.owner?.getAvatarUrl()?.let {
            binding.ivPodcastAvatar.loadCircleImage(it)
            //binding.ivAvatarImageListening.loadCircleImage(it)
        }

        cast.imageLinks?.large?.let {
            binding.ivPodcastBackground.loadImage(it)
        }

        //Handling the color background for podcast
        CommonsKt.handleColorFeed(cast, binding.colorFeedText, requireContext())
    }

    private fun setOnClicks() {

        binding.closeImageView.setOnClickListener {
            dismiss()
        }

        binding.tvPodcastUserName.setOnClickListener {
            openUserProfile()
        }
        binding.ivPodcastAvatar.setOnClickListener {
            openUserProfile()
        }

        binding.sharesLayout.throttledClick {
            onShareClick(cast) { shareResult ->
                cast.updateShares(shareResult)
                initShareState()
            }
        }

        binding.btnPlayStopPreview.setOnClickListener {
            playingPreview = !playingPreview
            binding.btnPlayStopPreview.text = if (playingPreview) "Stop" else "Preview"
            onPlayPreviewClick(playingPreview)
            cast.patronDetails?.previewDuration.let {
                if (it != null) {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        playingPreview = !playingPreview
                        binding.btnPlayStopPreview.text = if (playingPreview) "Stop" else "Preview"
                    }, it.toLong())
                }
            }
        }

    }

    private fun openUserProfile() {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, cast.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, cast.owner?.id)
        context?.startActivity(userProfileIntent)
    }

    private fun initLikeState() {
        fun applyLikeStyle(isLiked: Boolean) {
            binding.tvPodcastLikes.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isLiked) R.color.textAccent else R.color.white
                )
            )
        }

        binding.apply {
            applyLikeStyle(cast.isLiked!!)
            btnPodcastLikes.isLiked = cast.isLiked!!

            likeLayout.setOnClickListener {
                btnPodcastLikes.isLiked = !btnPodcastLikes.isLiked
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text =
                    (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                if (isLiked) {
                    Sounds.playSound(requireContext(), SoundType.HEART)
                }
                likePodcastViewModel.likeCast(cast.id, isLiked)
            }
        }
    }

    private fun initRecastState() {
        fun applyRecastState(isRecasted: Boolean) {
            binding.tvPodcastRecast.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isRecasted) R.color.textAccent else R.color.white
                )
            )
        }
        binding.apply {
            applyRecastState(cast.isRecasted!!)
            btnPodcastRecast.recasted = cast.isRecasted!!

            recastLayout.setOnClickListener {
                val isRecasted = !btnPodcastRecast.recasted
                val recastCount = binding.tvPodcastRecast.text.toString().toInt()

                applyRecastState(isRecasted)
                binding.tvPodcastRecast.text =
                    (if (isRecasted) recastCount + 1 else recastCount - 1).toString()
                binding.btnPodcastRecast.recasted = isRecasted

                if (isRecasted) {
                    Sounds.playSound(requireContext(), SoundType.RECAST)
                    recastPodcastViewModel.reCast(cast.id)
                } else {
                    recastPodcastViewModel.deleteRecast(cast.id)
                }
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

    private fun initShareState() {
        binding.tvPodcastReply.text = cast.sharesCount.toString()
        binding.btnPodcastReply.shared = cast.isShared == true
        applySharedState(cast.isShared == true)
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
        val priceId = cast.patronDetails?.priceId ?: return
        val details = productDetails
        if (details == null || details.productId != priceId) {
            playBillingHandler.getPrice(priceId, this)
            return
        }
        binding.btnBuyCast.text =
            "${getString(R.string.buy_cast)}\n${details.oneTimePurchaseOfferDetails?.formattedPrice}"
        binding.btnEditPrice.text =
            "${getString(R.string.edit_price)}\n${details.oneTimePurchaseOfferDetails?.formattedPrice}"
    }

    val onShareClick = { cast: CastUIModel, onShared: (shareResult: ShareResult) -> Unit ->
        ShareDialog.newInstance(cast).also { fragment ->
            fragment.setOnSharedListener(onShared)
            fragment.show(parentFragmentManager, fragment.requireTag())
        }
    }

    val onHashTagClick: (TagUIModel) -> Unit = { hashtag: TagUIModel ->
        (activity as? PlayerViewManager)?.navigateToHashTag(hashtag)
    }

    val onUserMentionClick: (String, Int) -> Unit = { username: String, userId: Int ->
        context?.let { context -> UserProfileActivity.show(context, username, userId) }
    }

    val onPlayPreviewClick = { play: Boolean ->
        cast.audio?.mapToAudioTrack()?.let { it1 ->
            cast.patronDetails?.startsAt?.let { it2 ->
                cast.patronDetails?.endsAt?.let { it3 ->
                    if (play) {
                        (activity as? PlayerViewManager)?.playPreview(
                            it1, it2, it3
                        )
                    } else {
                        (activity as? PlayerViewManager)?.stopPreview(true)
                    }
                }
            }
        }
    }

    private fun fetchDetails() {
        val priceId = cast.patronDetails?.priceId ?: return
        val details = productDetails
        if (details == null || details.productId != priceId) {
            playBillingHandler.getPrice(priceId, this)
            return
        }
    }

    override fun onDetailsAvailable(details: Map<String, ProductDetails>) {
        val priceId = cast.patronDetails?.priceId ?: return
        if (details.containsKey(priceId)) {
            productDetails = details[priceId]
            setPricingLabel()
        }
    }

    private fun launchPurchaseCast(cast: CastUIModel, productDetails: ProductDetails?) {
        val product = productDetails ?: return
        val purchaseTarget = PurchaseTarget(product, cast)
        playBillingHandler.launchBillingFlowFor(purchaseTarget, requireActivity()) { success ->
            if (success) {
                lifecycleScope.launch {
                    if(parentCommentId != -1){
                        RootCommentsFragment.newInstance(cast, parentCommentId, childCommentId).also { fragment ->
                            activity?.let { fragment.show(it.supportFragmentManager, fragment.requireTag()) }
                        }
                        dismiss()
                    } else{
                        (activity as MainActivityNew).openExtendedPlayer(cast.id)
                        dismiss()
                    }
                }
            }
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