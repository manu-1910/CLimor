package com.limor.app.scenes.main.fragments.discover2.common.casts

import android.view.View
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverBigCastBinding
import com.limor.app.scenes.main.fragments.discover2.common.casts.GridCastItemDecoration.Companion.GRID_CAST_ITEM
import com.limor.app.scenes.main.fragments.discover2.common.casts.GridCastItemDecoration.Companion.GRID_CAST_ITEM_TYPE_KEY
import com.limor.app.scenes.main.fragments.discover2.common.mock.MockCast
import com.limor.app.scenes.utils.DateUiUtil
import com.xwray.groupie.viewbinding.BindableItem
import java.time.Duration

class BigCastItem(
    val cast: MockCast,
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

            authorName.text = cast.owner.name
            val dateAndLocationText = "${
                DateUiUtil.getPastDateDaysTextDescription(
                    cast.date,
                    root.context
                )
            } - ${cast.location}"
            dateLocation.text = dateAndLocationText
            castName.text = cast.name
            castDuration.text = getCastDuration(cast.duration)

            Glide.with(root)
                .load(cast.imageUrl)
                .into(castImage)

            Glide.with(root)
                .load(cast.owner.imageUrl)
                .circleCrop()
                .into(ownerIcon)

            root.setOnClickListener {
                TODO()
            }
            moreBtn.setOnClickListener {
                TODO()
            }
        }
    }

    private fun getCastDuration(duration: Duration): String {
        val minutes = duration.toMinutes()
        return String.format("%dm %ds", minutes, duration.minusMinutes(minutes).seconds)
    }

    override fun getLayout() = R.layout.item_discover_big_cast
    override fun initializeViewBinding(view: View) = ItemDiscoverBigCastBinding.bind(view)

    override fun getSpanSize(spanCount: Int, position: Int) = spanSize
}
