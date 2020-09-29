package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.square1.limor.R
import io.square1.limor.scenes.utils.Commons
import io.square1.limor.uimodels.UIPodcast
import org.jetbrains.anko.sdk23.listeners.onClick

class TopCastViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val topCastClickListener: TopCastAdapter.OnTopCastClicked,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.top_cast_item,
        parent,
        false
    )
) {

    private var rlRoot: RelativeLayout = itemView.findViewById(R.id.rl_root)
    private var ivBackground: ImageView = itemView.findViewById(R.id.iv_background)
    private var ivPlay: ImageView = itemView.findViewById(R.id.iv_play_pause)
    private var tvPodcastTitle: TextView = itemView.findViewById(R.id.tv_podcast_title)
    private var tvPodcastLength: TextView = itemView.findViewById(R.id.tv_podcast_length)


    fun bind(currentItem: UIPodcast, position: Int) {

        rlRoot.onClick { topCastClickListener.onTopCastItemClicked(currentItem, position) }
        ivPlay.onClick { topCastClickListener.onTopCastPlayClicked(currentItem, position) }

        Glide.with(context)
            .load(currentItem.images.large_url)
            .placeholder(R.drawable.limor_orange_primary)
            .into(ivBackground)

        tvPodcastTitle.text = currentItem.caption
        tvPodcastLength.text = Commons.getHumanReadableTimeFromMillis(currentItem.audio.duration)

    }


}