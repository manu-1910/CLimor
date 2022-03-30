package com.limor.app.scenes.main.fragments.profile.casts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import com.android.billingclient.api.SkuDetails
import com.limor.app.R
import com.limor.app.databinding.ItemUserCastBinding
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.service.DetailsAvailableListener
import com.limor.app.service.ProductDetails
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

class CastItem(
    val userId: Int,
    val binding: ItemUserCastBinding,
    private val onCastClick: (CastUIModel, SkuDetails?) -> Unit,
    private val onLikeClick: (CastUIModel, like: Boolean) -> Unit,
    private val onMoreDialogClick: (CastUIModel) -> Unit,
    private val onRecastClick: (CastUIModel, isRecasted: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel, SkuDetails?) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit,
    private val isPurchased: Boolean = false,
    private val onPurchaseCast: (cast: CastUIModel, sku: SkuDetails?) -> Unit,
    private val onEditPreviewClick: (cast: CastUIModel) -> Unit,
    private val onPlayPreviewClick: (cast: CastUIModel, play: Boolean) -> Unit,
    private val onEditPriceClick: (cast: CastUIModel) -> Unit,
    private val productDetailsFetcher: ProductDetails? = null,
) : ViewHolderBindable<CastUIModel>(binding), DetailsAvailableListener {

    private var skuDetails: SkuDetails? = null
    private lateinit var cast: CastUIModel
    private var playingPreview = false

    override fun bind(item: CastUIModel) {
        binding.apply {
            cast = item
            binding.matureContentInfo.visibility = if (cast.maturedContent == true)
                View.VISIBLE
            else
                View.GONE


            tvPodcastUserName.text = cast.owner?.username
            tvPodcastUserSubtitle.text = cast.getCreationDateAndPlace(root.context, true)
            ivVerifiedAvatar.visibility =
                if (cast.owner?.isVerified == true) View.VISIBLE else View.GONE

            tvPodcastLength.text = cast.audio?.duration?.let { CommonsKt.getFeedDuration(it) }

            cast.owner?.getAvatarUrl()?.let {
                ivPodcastAvatar.loadCircleImage(it)
                ivAvatarImageListening.loadCircleImage(it)
            }

            cast.imageLinks?.large?.let {
                ivPodcastBackground.loadImage(it)
            }

            //Handling the color background for podcast
            CommonsKt.handleColorFeed(cast, colorFeedText, root.context)

            tvPodcastLikes.text = cast.likesCount.toString()
            tvPodcastRecast.text = cast.recastsCount.toString()
            tvPodcastComments.text = cast.commentsCount.toString()
            tvPodcastReply.text = cast.sharesCount.toString()
            tvPodcastNumberOfListeners.text =
                if (cast.listensCount == 0) "0" else cast.listensCount?.toLong()?.formatHumanReadable

            initRecastState(binding, cast)

            cpiPodcastListeningProgress.progress = 50 // TODO change to the real value

            tvPodcastTitle.text = cast.title

            tvPodcastSubtitle.setTextWithTagging(
                cast.caption,
                cast.mentions,
                cast.tags,
                { username, userId ->
                    root.context?.let { context ->
                        UserProfileActivity.show(
                            context,
                            username,
                            userId
                        )
                    }
                },
                onHashTagClick
            )

            initLikeState(binding, cast)

            btnPodcastReply.shared = cast.isShared == true

            clItemPodcastFeed.setOnClickListener {
                onCastClick(cast, skuDetails)
            }

            btnPodcastComments.throttledClick {
                onCommentsClick(cast, skuDetails)
            }

            tvPodcastComments.throttledClick {
                onCommentsClick(cast, skuDetails)
            }

            binding.btnAddPreview.setOnClickListener {
                onEditPreviewClick(cast)
            }

            binding.btnEditPrice.setOnClickListener {
                onEditPriceClick(cast)
            }

            ivPodcastAvatar.setOnClickListener {
                if(cast.owner?.id != userId){
                    openUserProfile(cast)
                }
            }

            ivAvatarImageListening.setOnClickListener {
                if(cast.owner?.id != userId){
                    openUserProfile(cast)
                }
            }

            tvPodcastUserName.setOnClickListener {
                if(cast.owner?.id != userId){
                    openUserProfile(cast)
                }
            }

            binding.btnPlayStopPreview.setOnClickListener {
                playingPreview = !playingPreview
                binding.btnPlayStopPreview.text = if (playingPreview) "Stop" else "Preview"
                onPlayPreviewClick(cast, playingPreview)
                cast.patronDetails?.previewDuration.let {
                    if (it != null) {
                        Handler().postDelayed(Runnable {
                            playingPreview = !playingPreview
                            binding.btnPlayStopPreview.text = if (playingPreview) "Stop" else "Preview"
                        }, it.toLong())
                    }
                }
            }

            sharesLayout.throttledClick {
                onShareClick(cast) { shareResult ->
                    cast.updateShares(shareResult)
                    tvPodcastReply.text = cast.sharesCount.toString()
                    btnPodcastReply.shared = cast.isShared == true
                    applyShareStyle(binding, cast.isShared == true)
                }
            }

            setPatronPodcastStatus(cast, binding)

        }

    }

    private fun openUserProfile(item: CastUIModel) {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, item.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, item.owner?.id)
        context.startActivity(userProfileIntent)
    }

    private fun initRecastState(binding: ItemUserCastBinding, item: CastUIModel) {
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

                onRecastClick(item, isRecasted)
            }
        }
    }

    private fun applyShareStyle(binding: ItemUserCastBinding, isShared: Boolean) {
        // new requirement as of October 28th, 2021
        // - the share button shouldn't have state
        binding.tvPodcastReply.setTextColor(ContextCompat.getColor(
            binding.tvPodcastReply.context,
            R.color.white
        ))
    }

    private fun initLikeState(binding: ItemUserCastBinding, cast: CastUIModel) {
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
            btnPodcastLikes.isLiked = cast.isLiked

            likeLayout.setOnClickListener {
                btnPodcastLikes.isLiked = !btnPodcastLikes.isLiked
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text =
                    (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                onLikeClick(cast, isLiked)
            }
        }
    }

    /*override fun isSameAs(other: Item<*>): Boolean {
        if (other !is CastItem) return false
        return cast.id == other.cast.id
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        if (other !is CastItem) return false
        return cast == other.cast
    }

    override fun getLayout() = R.layout.item_user_cast
    override fun initializeViewBinding(view: View) = ItemUserCastBinding.bind(view)*/


    private fun setPatronPodcastStatus(item: CastUIModel, binding: ItemUserCastBinding) {
        if (item.patronCast == true) {

            val userId = PrefsHandler.getCurrentUserId(binding.root.context)
            binding.patronCastIndicator.visibility = View.VISIBLE

            binding.btnBuyCast.setOnClickListener {
                skuDetails?.let {
                    onPurchaseCast(item, it)
                }
            }

            when {
                (item.owner?.id != userId) -> {
                    Timber.d("Cast Item not owner -> $item")

                    if (cast.patronDetails?.purchased == true) {
                        binding.btnPurchasedCast.visibility = View.VISIBLE
                        binding.notCastOwnerActions.visibility = View.GONE
                        binding.castOwnerActions.visibility = View.GONE
                        binding.btnPurchasedCast.text =
                            "Purchased at ${cast.patronDetails?.castPurchasedDetails?.purchased_in_currency} ${cast.patronDetails?.castPurchasedDetails?.purchased_at_price} "
                    } else {
                        //Purchase a cast actions
                        binding.notCastOwnerActions.visibility = View.VISIBLE
                        binding.btnAddPreview.visibility = View.GONE
                        binding.btnEditPrice.visibility = View.GONE
                        binding.btnPurchasedCast.visibility = View.GONE
                        setPricingLabel()
                    }

                }
                item.owner.id == userId -> {
                    //Self Patron Cast
                    binding.btnPlayStopPreview.visibility = View.GONE
                    binding.btnBuyCast.visibility = View.GONE
                    binding.castOwnerActions.visibility = View.VISIBLE
                    binding.btnPurchasedCast.visibility = View.GONE
                    setPricingLabel()
                }
                else -> {
                    binding.notCastOwnerActions.visibility = View.GONE
                    binding.castOwnerActions.visibility = View.GONE
                    binding.btnPurchasedCast.visibility = View.VISIBLE

                }
            }
        } else {
            binding.btnPurchasedCast.visibility = View.GONE
            binding.notCastOwnerActions.visibility = View.GONE
            binding.castOwnerActions.visibility = View.GONE
            binding.btnPurchasedCast.visibility = View.GONE
            binding.patronCastIndicator.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPricingLabel() {
        val priceId = cast.patronDetails?.priceId ?: return
        val details = skuDetails
        if (details == null || details.sku != priceId) {
            productDetailsFetcher?.getPrice(priceId, this)
            return
        }
        binding.btnBuyCast.text = "${context.getString(R.string.buy_cast)}\n${details.price}"
        binding.btnEditPrice.text = "${context.getString(R.string.edit_price)}\n${details.price}"
    }

    override fun onDetailsAvailable(details: Map<String, SkuDetails>) {
        val priceId = cast.patronDetails?.priceId ?: return
        if (details.containsKey(priceId)) {
            skuDetails = details[priceId]
            setPricingLabel()
        }
    }

}