package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.square1.limor.R
import io.square1.limor.scenes.utils.CommonsKt.Companion.calculateDurationMinutesAndSeconds
import io.square1.limor.scenes.utils.CommonsKt.Companion.getDateTimeFormattedFromTimestamp
import io.square1.limor.uimodels.UIFeedItem
import java.util.*
import kotlin.collections.ArrayList


class FeedAdapter(
    var context: Context,
    list: ArrayList<UIFeedItem>
) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIFeedItem> = ArrayList()


    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedAdapter.ViewHolder {
        val view = inflator.inflate(R.layout.fragment_feed_item_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: FeedAdapter.ViewHolder, position: Int) {
        val currentItem = list[position]

        // fullname
        val fullname: String = currentItem.user.first_name + " " + currentItem.user.last_name
        holder.tvUserFullname.text = fullname


        // datetime and location
        val lat = currentItem.podcast?.latitude
        val lng = currentItem.podcast?.longitude
        var locationString = ""
        if (lat != null && lng != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(lat, lng, 1)
            if (addresses.isNotEmpty()) {
                val cityName: String = addresses[0].locality
                val countryName: String = addresses[0].countryName
                locationString = " - $cityName, $countryName"
            }
        }
        val datetimeString = getDateTimeFormattedFromTimestamp(currentItem.created_at.toLong())
        val dateAndLocationString = "$datetimeString$locationString"
        holder.tvDateAndLocation.text = dateAndLocationString

        // title & caption
        holder.tvPodcastTitle.text = currentItem.podcast?.title
        holder.tvPodcastText.text = currentItem.podcast?.caption

        // duration
        val duration = currentItem.podcast?.audio?.duration
        if (duration != null)
            holder.tvPodcastTime.text = calculateDurationMinutesAndSeconds(duration.toLong())

        holder.tvRecasts.text = currentItem.podcast?.number_of_recasts!!.toString()
        holder.tvLikes.text = currentItem.podcast?.number_of_likes!!.toString()
        holder.tvListens.text = currentItem.podcast?.number_of_listens!!.toString()
        holder.tvComments.text = currentItem.podcast?.number_of_comments!!.toString()

        // user picture
        Glide.with(context)
            .load(currentItem.user.images.small_url)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.ivUser)

        // main picture
        Glide.with(context)
            .load(currentItem.podcast?.images?.medium_url)
            .into(holder.ivMainFeedPicture)

        // verified
        if (currentItem.user.verified)
            holder.ivVerifiedUser.visibility = View.VISIBLE
        else
            holder.ivVerifiedUser.visibility = View.GONE

        // like
        currentItem.podcast?.liked?.let {
            if(it)
                holder.ibtnLike.setImageResource(R.drawable.like_filled)
            else
                holder.ibtnLike.setImageResource(R.drawable.like)
        } ?: run {
            holder.ibtnLike.setImageResource(R.drawable.like)
        }

        // recast
        currentItem.podcast?.recasted?.let {
            if(it)
                holder.ibtnRecasts.setImageResource(R.drawable.recast_filled)
            else
                holder.ibtnRecasts.setImageResource(R.drawable.recast)
        } ?: run {
            holder.ibtnRecasts.setImageResource(R.drawable.recast)
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserFullname: TextView
        var tvDateAndLocation: TextView
        var tvPodcastTitle: TextView
        var tvPodcastText: TextView
        var tvPodcastTime: TextView

        var tvListens: TextView
        var tvComments: TextView
        var tvLikes: TextView
        var tvRecasts: TextView

        var ivUser: ImageView
        var ivMainFeedPicture: ImageView
        var ivVerifiedUser: ImageView

        var ibtnLike : ImageButton
        var ibtnRecasts : ImageButton


        init {
            tvUserFullname = itemView.findViewById(R.id.tvUserName)
            tvDateAndLocation = itemView.findViewById(R.id.tvTimeAndLocation)
            tvPodcastTitle = itemView.findViewById(R.id.tvPodcastTitle)
            tvPodcastText = itemView.findViewById(R.id.tvPodcastText)
            tvPodcastTime = itemView.findViewById(R.id.tvPodcastTime)

            tvListens = itemView.findViewById(R.id.tvListens)
            tvComments = itemView.findViewById(R.id.tvComments)
            tvLikes = itemView.findViewById(R.id.tvLikes)
            tvRecasts = itemView.findViewById(R.id.tvRecasts)

            ivUser = itemView.findViewById(R.id.ivUserPicture)
            ivMainFeedPicture = itemView.findViewById(R.id.ivMainFeedPicture)
            ivVerifiedUser = itemView.findViewById(R.id.ivVerifiedUser)

            ibtnLike = itemView.findViewById(R.id.btnLikes)
            ibtnRecasts = itemView.findViewById(R.id.btnRecasts)
        }

    }


}