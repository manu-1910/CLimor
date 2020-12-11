package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.uimodels.UIPodcast

class PodcastGridViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val listener: PodcastsGridAdapter.OnPodcastClickListener,
    val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_podcast_grid_item,
        parent,
        false
    )
) {


    private var ivUser: ImageView = itemView.findViewById(R.id.ivUserPicture)
    private var tvUsername: TextView = itemView.findViewById(R.id.tvUserName)
    private var tvTimeAndLocation: TextView = itemView.findViewById(R.id.tvTimeAndLocation)
    private var tvTimePodcast: TextView = itemView.findViewById(R.id.tvTimePodcast)
    private var tvTitlePodcast: TextView = itemView.findViewById(R.id.tvTitlePodcast)
    private var ivPlayPodcast: ImageView = itemView.findViewById(R.id.ivPlayPodcast)
    private var btnMore: AppCompatImageButton = itemView.findViewById(R.id.btnMore)

    fun bind(currentItem: UIPodcast, position: Int) {
        tvUsername.text = currentItem.user.username
        tvTitlePodcast.text = currentItem.title
    }

}