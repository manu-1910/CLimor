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

class FeaturedItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val featuredClickListener: FeaturedItemAdapter.OnFeaturedClicked,
    private val context: Context,
    itemWidth: Int
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.featured_cast_item,
        parent,
        false
    )
) {

    init {
        val params = itemView.layoutParams
        params.width = itemWidth
    }

    private var tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
    private var tvDateLocation: TextView = itemView.findViewById(R.id.tv_date_location)
    private var ivUser: ImageView = itemView.findViewById(R.id.iv_user)
    private var rlRoot: RelativeLayout = itemView.findViewById(R.id.rl_root)
    private var ivBackground: ImageView = itemView.findViewById(R.id.iv_background)
    private var ivMore: ImageView = itemView.findViewById(R.id.iv_more)
    private var ivPlay: ImageView = itemView.findViewById(R.id.iv_play_pause)
    private var tvPodcastTitle: TextView = itemView.findViewById(R.id.tv_podcast_title)
    private var tvPodcastLength: TextView = itemView.findViewById(R.id.tv_podcast_length)


    fun bind(currentItem: UIPodcast, position: Int) {

        rlRoot.onClick { featuredClickListener.onFeaturedItemClicked(currentItem, position) }
        ivMore.onClick { featuredClickListener.onMoreClicked(currentItem, position, itemView) }
        ivPlay.onClick { featuredClickListener.onPlayClicked(currentItem, position) }

        Glide.with(context)
            .load(currentItem.user.images.small_url)
            .placeholder(R.drawable.hashtag)
            .circleCrop()
            .into(ivUser)

        Glide.with(context)
            .load(currentItem.images.large_url)
            .placeholder(R.drawable.limor_orange_primary)
            .into(ivBackground)

        tvUserName.text = currentItem.user.username
        tvDateLocation.text = Commons.getDatePlusHourMinutesFromDateInt(currentItem.created_at)

        tvPodcastTitle.text = currentItem.caption

        tvPodcastLength.text = Commons.getHumanReadableTimeFromMillis(currentItem.audio.total_length.toInt())
//        tvPodcastLength.text = Commons.getHumanReadableTimeFromMillis(currentItem.audio.duration)

    }


}