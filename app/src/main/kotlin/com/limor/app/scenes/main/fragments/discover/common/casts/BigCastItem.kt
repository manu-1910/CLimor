package com.limor.app.scenes.main.fragments.discover.common.casts

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverBigCastBinding
import com.limor.app.extensions.getActivity
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration.Companion.GRID_CAST_ITEM
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration.Companion.GRID_CAST_ITEM_TYPE_KEY
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import java.time.Duration

class BigCastItem(
    val cast: CastUIModel,
    private val width: Int = -1,
    private val spanSize: Int = 2
) : BindableItem<ItemDiscoverBigCastBinding>() {

    init {
        extras[GRID_CAST_ITEM_TYPE_KEY] = GRID_CAST_ITEM
    }

    override fun bind(viewBinding: ItemDiscoverBigCastBinding, position: Int) {
        viewBinding.apply {
            if (this@BigCastItem.width != -1) {
                root.updateLayoutParams { width = this@BigCastItem.width }
            }

            authorName.text = cast.owner?.username
            ivVerifiedAvatar.visibility = if(cast.owner?.isVerified == true) View.VISIBLE else View.GONE
            dateLocation.text = cast.getCreationDateAndPlace(root.context, true)
            castName.text = cast.title
            cast.audio?.duration?.let {
                castDuration.text = CommonsKt.getFeedDuration(cast.audio.duration)
            }

            if(cast.imageLinks?.medium!=null){
                Glide.with(root)
                    .load(cast.imageLinks.medium)
                    .into(castImage)
            }else{
                castImage.setBackgroundColor(Color.parseColor(cast.colorCode))
            }

            Glide.with(root)
                .load(cast.owner?.getAvatarUrl())
                .signature(ObjectKey(cast.owner?.getAvatarUrl() ?: ""))
                .error(R.drawable.ic_default_avatar)
                .placeholder(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(ownerIcon)

            root.setOnClickListener {
                (it.context.getActivity() as? PlayerViewManager)?.showExtendedPlayer(cast.id)
            }

            moreBtn.setOnClickListener {
                ToastMaker.showToast(it.context, "Not implemented")
            }

            authorName.setOnClickListener {
                openUserProfile(this)
            }
            ownerIcon.setOnClickListener {
                openUserProfile(this)
            }
        }
    }

    private fun openUserProfile(viewBinding: ItemDiscoverBigCastBinding) {
        val userProfileIntent = Intent(viewBinding.root.context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, cast.owner?.username)
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY,cast.owner?.id)
        viewBinding.root.context.startActivity(userProfileIntent)
    }

    private fun getCastDuration(duration: Duration): String {
        val minutes = duration.toMinutes()
        return String.format("%dm %ds", minutes, duration.minusMinutes(minutes).seconds)
    }

    override fun getLayout() = R.layout.item_discover_big_cast
    override fun initializeViewBinding(view: View) = ItemDiscoverBigCastBinding.bind(view)

    override fun getSpanSize(spanCount: Int, position: Int) = spanSize

    override fun isSameAs(other: Item<*>): Boolean {
        if (other is BigCastItem) {
            return other == this
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BigCastItem

        if (cast != other.cast) return false
        if (width != other.width) return false
        if (spanSize != other.spanSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cast.hashCode()
        result = 31 * result + width
        result = 31 * result + spanSize
        return result
    }
}
