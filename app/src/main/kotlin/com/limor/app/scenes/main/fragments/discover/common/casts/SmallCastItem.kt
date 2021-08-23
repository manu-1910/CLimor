package com.limor.app.scenes.main.fragments.discover.common.casts

import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSmallCastBinding
import com.limor.app.extensions.getActivity
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import java.time.Duration

class SmallCastItem(
    val cast: CastUIModel,
    private val spanSize: Int = 1
) : BindableItem<ItemDiscoverSmallCastBinding>() {

    init {
        extras[GridCastItemDecoration.GRID_CAST_ITEM_TYPE_KEY] =
            GridCastItemDecoration.GRID_CAST_ITEM
    }

    override fun bind(viewBinding: ItemDiscoverSmallCastBinding, position: Int) {
        viewBinding.apply {
            authorName.text = cast.owner?.username
            castName.text = cast.title
            cast.audio?.duration?.let {
                castDuration.text = getCastDuration(cast.audio.duration)
            }

            Glide.with(root)
                .load(cast.imageLinks?.medium)
                .into(castImage)

            Glide.with(root)
                .load(cast.owner?.imageLinks?.small)
                .circleCrop()
                .into(ownerIcon)

            root.setOnClickListener {
                (it.context.getActivity() as? PlayerViewManager)?.showExtendedPlayer(cast.id)
            }

            authorName.setOnClickListener {
                openUserProfile(this)
            }

            ownerIcon.setOnClickListener {
                openUserProfile(this)
            }

        }
    }

    private fun openUserProfile(viewBinding: ItemDiscoverSmallCastBinding) {
        val userProfileIntent =
            Intent(viewBinding.root.context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, cast.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, cast.owner?.id)
        viewBinding.root.context.startActivity(userProfileIntent)
    }

    private fun getCastDuration(duration: Duration): String {
        val minutes = duration.toMinutes()
        return String.format("%dm %ds", minutes, duration.minusMinutes(minutes).seconds)
    }

    override fun getLayout() = R.layout.item_discover_small_cast
    override fun initializeViewBinding(view: View) = ItemDiscoverSmallCastBinding.bind(view)
    override fun getSpanSize(spanCount: Int, position: Int) = spanSize

    override fun isSameAs(other: Item<*>): Boolean {
        if (other is SmallCastItem) {
            return other == this
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SmallCastItem

        if (cast != other.cast) return false
        if (spanSize != other.spanSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cast.hashCode()
        result = 31 * result + spanSize
        return result
    }
}
