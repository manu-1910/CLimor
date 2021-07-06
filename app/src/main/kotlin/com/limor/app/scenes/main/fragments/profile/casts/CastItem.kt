package com.limor.app.scenes.main.fragments.profile.casts

import android.view.View
import androidx.core.content.ContextCompat
import com.limor.app.R
import com.limor.app.databinding.ItemUserCastBinding
import com.limor.app.extensions.dp
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.extensions.px
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.scenes.utils.recycler.HorizontalSpacingItemDecoration
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import kotlin.time.seconds

class CastItem(
    val cast: CastUIModel,
    private val onCastClick: (CastUIModel) -> Unit,
    private val onLikeClick: (CastUIModel, like: Boolean) -> Unit,
    private val onMoreDialogClick: (CastUIModel) -> Unit,
) : BindableItem<ItemUserCastBinding>() {

    override fun bind(viewBinding: ItemUserCastBinding, position: Int) {
        viewBinding.apply {
            tvPodcastUserName.text = cast.owner?.getFullName()
            val dateAndLocationText = "${
                cast.createdAt?.let {
                    DateUiUtil.getPastDateDaysTextDescription(
                        cast.createdAt,
                        root.context
                    )
                }
            } - ${cast.address}"
            tvPodcastUserSubtitle.text = dateAndLocationText

            tvPodcastLength.text = cast.audio?.duration?.let {
                "${it.toMinutes()}m ${it.minusMinutes(it.toMinutes()).seconds}s"
            }

            cast.owner?.imageLinks?.small?.let {
                ivPodcastAvatar.loadCircleImage(it)
                ivAvatarImageListening.loadCircleImage(it)
            }

            cast.imageLinks?.medium?.let {
                ivPodcastBackground.loadImage(it)
            }

            tvPodcastLikes.text = cast.likesCount.toString()
            tvPodcastRecast.text = cast.recastsCount.toString()
            tvPodcastComments.text = cast.commentsCount.toString()
            tvPodcastReply.text = cast.sharesCount.toString()
            tvPodcastNumberOfListeners.text = cast.listensCount.toString()

            cpiPodcastListeningProgress.progress = 50 // TODO change to the real value

            cast.tags?.let { tagsList ->
                if (tagsHorizontalList.itemDecorationCount == 0) {
                    tagsHorizontalList.addItemDecoration(
                        HorizontalSpacingItemDecoration(
                            spacing = 12.px,
                            includeFirstItem = false,
                            includeLastItem = false
                        )
                    )
                }
                tagsHorizontalList.adapter = GroupieAdapter().apply {
                    addAll(
                        tagsList.map { TagItem(it) }
                    )
                }
            }

            tvPodcastTitle.text = cast.title
            tvPodcastSubtitle.text = cast.caption

            initLikeState(viewBinding, cast)

            clItemPodcastFeed.setOnClickListener {
                onCastClick(cast)
            }

            btnPodcastMore.setOnClickListener {
                onMoreDialogClick(cast)
            }
        }
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

            btnPodcastLikes.setOnClickListener {
                val isLiked = btnPodcastLikes.isLiked
                val likesCount = binding.tvPodcastLikes.text.toString().toInt()

                applyLikeStyle(isLiked)
                binding.tvPodcastLikes.text = (if (isLiked) likesCount + 1 else likesCount - 1).toString()

                onLikeClick(cast, isLiked)
            }
        }
    }

    override fun getLayout() = R.layout.item_user_cast
    override fun initializeViewBinding(view: View) = ItemUserCastBinding.bind(view)
}