package com.limor.app.scenes.main.adapters

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.R
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIPodcast
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class PodcastGridLargeViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val listener: PodcastsGridAdapter.OnPodcastClickListener,
    val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_podcast_large_grid_item,
        parent,
        false
    )
) {

    private var ivUser: ImageView = itemView.findViewById(R.id.ivUserPicture)
    private var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
    private var tvUsername: TextView = itemView.findViewById(R.id.tvUserName)
    private var tvTimeAndLocation: TextView = itemView.findViewById(R.id.tvTimeAndLocation)
    private var tvTimePodcast: TextView = itemView.findViewById(R.id.tvTimePodcast)
    private var tvTitlePodcast: TextView = itemView.findViewById(R.id.tvTitlePodcast)
    private var ivPlayPodcast: ImageView = itemView.findViewById(R.id.ivPlayPodcast)
    private var btnMore: AppCompatImageButton = itemView.findViewById(R.id.btnMore)

    fun bind(currentItem: UIPodcast, position: Int) {
        tvUsername.text = currentItem.user.username
        tvTitlePodcast.text = currentItem.title


        tvTimePodcast.text =
            CommonsKt.calculateDurationMinutesAndSeconds(currentItem.audio.total_length.toLong())
        tvTimeAndLocation.text = getTextTimeAndLocation(currentItem)


        // user picture
        Glide.with(itemView.context)
            .load(currentItem.user.images.small_url)
            .placeholder(R.mipmap.ic_launcher_round)
            .apply(RequestOptions.circleCropTransform())
            .error(R.mipmap.ic_launcher_round)
            .into(ivUser)

        // podcast picture
        Glide.with(itemView.context)
            .load(currentItem.images.original_url)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .into(ivPicture)

        ivPicture.onClick { listener.onItemClicked(currentItem, position) }
        ivUser.onClick { listener.onUserClicked(currentItem, position) }
        tvUsername.onClick { listener.onUserClicked(currentItem, position) }
        btnMore.onClick { listener.onMoreClicked(currentItem, position, btnMore) }
        ivPlayPodcast.onClick { listener.onPlayClicked(currentItem, position) }
    }

    private fun getTextTimeAndLocation(currentItem: UIPodcast): String {
        // datetime and location
        val lat = currentItem.latitude
        val lng = currentItem.longitude
        var locationString = ""
        if (lat != null && lng != null) {
            val geocoder = Geocoder(itemView.context, Locale.getDefault())
            var addresses: List<Address> = ArrayList()
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1)
            } catch (e: Exception) {
                Timber.d("Couldn't get location from geocoder")
            }
            if (addresses.isNotEmpty()) {
                if (addresses[0].locality != null && addresses[0].countryName != null) {
                    val cityName: String = addresses[0].locality
                    val countryName: String = addresses[0].countryName
                    locationString = " - $cityName, $countryName"
                }
            }
        }
        val datetimeString =
            CommonsKt.getDateTimeFormattedFromTimestamp(currentItem.created_at.toLong())
        return "$datetimeString$locationString"
    }

}