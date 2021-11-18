package com.limor.app.scenes.main.fragments.profile.casts

import android.view.View
import androidx.core.content.ContextCompat
import com.limor.app.R
import com.limor.app.databinding.ItemUserCastBinding
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.*
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.utils.CommonsKt.Companion.toEditable
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.recycler.HorizontalSpacingItemDecoration
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.android.synthetic.main.activity_edit_cast.*

class CastItem(
    val cast: CastUIModel,
    private val onCastClick: (CastUIModel) -> Unit,
    private val onLikeClick: (CastUIModel, like: Boolean) -> Unit,
    private val onMoreDialogClick: (CastUIModel) -> Unit,
    private val onRecastClick: (CastUIModel, isRecasted: Boolean) -> Unit,
    private val onCommentsClick: (CastUIModel) -> Unit,
    private val onShareClick: (CastUIModel, onShared: ((shareResult: ShareResult) -> Unit)?) -> Unit,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit
) : BindableItem<ItemUserCastBinding>() {

    override fun bind(viewBinding: ItemUserCastBinding, position: Int) {
        viewBinding.apply {
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

            if(cast.imageLinks == null){
                colorFeedState.visibility = View.VISIBLE
            }else{
                colorFeedState.visibility = View.GONE
            }

            tvPodcastLikes.text = cast.likesCount.toString()
            tvPodcastRecast.text = cast.recastsCount.toString()
            tvPodcastComments.text = cast.commentsCount.toString()
            tvPodcastReply.text = cast.sharesCount.toString()
            tvPodcastNumberOfListeners.text =
                if (cast.listensCount == 0) "0" else cast.listensCount?.toLong()?.formatHumanReadable

            initRecastState(viewBinding, cast)

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

            initLikeState(viewBinding, cast)

            btnPodcastReply.shared = cast.isShared == true

            clItemPodcastFeed.setOnClickListener {
                onCastClick(cast)
            }

            btnPodcastMore.setOnClickListener {
                onMoreDialogClick(cast)
            }

            btnPodcastComments.throttledClick {
                onCommentsClick(cast)
            }

            tvPodcastComments.throttledClick {
                onCommentsClick(cast)
            }

            sharesLayout.throttledClick {
                onShareClick(cast) { shareResult ->
                    cast.updateShares(shareResult)
                    tvPodcastReply.text = cast.sharesCount.toString()
                    btnPodcastReply.shared = cast.isShared == true
                    applyShareStyle(viewBinding, cast.isShared == true)
                }
            }
        }

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

    private fun applyShareStyle(binding: ItemUserCastBinding, isShared : Boolean){
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

    override fun isSameAs(other: Item<*>): Boolean {
        if (other !is CastItem) return false
        return cast.id == other.cast.id
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        if (other !is CastItem) return false
        return cast == other.cast
    }

    override fun getLayout() = R.layout.item_user_cast
    override fun initializeViewBinding(view: View) = ItemUserCastBinding.bind(view)
}