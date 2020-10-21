package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.uimodels.UITags
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*

class DiscoverHashTagsViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val hashtagSearchClickListener: DiscoverHashTagsAdapter.OnHashTagSearchClicked,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.discover_hashtag_item,
        parent,
        false
    )
) {


    private var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    private var tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
    private var rlRoot: RelativeLayout = itemView.findViewById(R.id.rl_root_view)

    fun bind(currentItem: UITags, position: Int) {

        rlRoot.onClick { hashtagSearchClickListener.onHashTagClicked(currentItem, position) }

        tvTitle.text = currentItem.text
        val castsText = currentItem.count.toString() + " " + context.getString(R.string.profile_casts_title).toLowerCase(
            Locale.ROOT
        )
        tvSubtitle.text = castsText

    }


}