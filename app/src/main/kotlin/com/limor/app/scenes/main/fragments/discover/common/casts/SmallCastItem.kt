package com.limor.app.scenes.main.fragments.discover.common.casts

import android.view.View
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSmallCastBinding
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.uimodels.CastUIModel
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
            authorName.text = cast.owner.getFullName()
            castName.text = cast.title
            castDuration.text = getCastDuration(cast.audio.duration)

            Glide.with(root)
                .load(cast.imageLinks.medium)
                .into(castImage)

            Glide.with(root)
                .load(cast.owner.imageLinks.small)
                .circleCrop()
                .into(ownerIcon)

            root.setOnClickListener {
                ToastMaker.showToast(it.context, "Not implemented")
            }
        }
    }

    private fun getCastDuration(duration: Duration): String {
        val minutes = duration.toMinutes()
        return String.format("%dm %ds", minutes, duration.minusMinutes(minutes).seconds)
    }

    override fun getLayout() = R.layout.item_discover_small_cast
    override fun initializeViewBinding(view: View) = ItemDiscoverSmallCastBinding.bind(view)
    override fun getSpanSize(spanCount: Int, position: Int) = spanSize
}
