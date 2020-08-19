package io.square1.limor.scenes.main.adapters

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
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
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIFeedItem
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class FeedItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.fragment_feed_item_recycler_view,
            parent,
            false
        )
    ) {
    var tvUserFullname: TextView
    var tvDateAndLocation: TextView
    var tvPodcastTitle: TextView
    var tvPodcastText: TextView
    var tvPodcastTime: TextView
    var tvSomeoneRecasted: TextView

    var tvListens: TextView
    var tvComments: TextView
    var tvLikes: TextView
    var tvRecasts: TextView

    var ivUser: ImageView
    var ivMainFeedPicture: ImageView
    var ivVerifiedUser: ImageView

    var ibtnLike: ImageButton
    var ibtnRecasts: ImageButton

    var clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(textView: View) {
            val tv = textView as TextView
            val s: Spanned = tv.text as Spanned
            val start: Int = s.getSpanStart(this)
            val end: Int = s.getSpanEnd(this)
            val clickedTag = s.subSequence(start, end).toString()

//            onTagClicked(clickedTag)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = true
        }
    }


    init {
        tvUserFullname = itemView.findViewById(R.id.tvUserName)
        tvDateAndLocation = itemView.findViewById(R.id.tvTimeAndLocation)
        tvPodcastTitle = itemView.findViewById(R.id.tvPodcastTitle)
        tvPodcastText = itemView.findViewById(R.id.tvPodcastText)
        tvPodcastTime = itemView.findViewById(R.id.tvPodcastTime)
        tvSomeoneRecasted = itemView.findViewById(R.id.tvSomeoneRecasted)

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

    fun bind(currentItem: UIFeedItem) {
        // fullname
        val fullname: String = currentItem.user.first_name + " " + currentItem.user.last_name
        tvUserFullname.text = fullname


        // datetime and location
        val lat = currentItem.podcast?.latitude
        val lng = currentItem.podcast?.longitude
        var locationString = ""
        if (lat != null && lng != null) {
            val geocoder = Geocoder(itemView.context, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(lat, lng, 1)
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
        val dateAndLocationString = "$datetimeString$locationString"
        tvDateAndLocation.text = dateAndLocationString

        // title & caption
        tvPodcastTitle.text = currentItem.podcast?.title
        tvPodcastText.text = fillCaption(currentItem.podcast?.caption)

        tvPodcastText.movementMethod = LinkMovementMethod.getInstance()
        tvPodcastText.highlightColor = Color.RED

        // duration
        val duration = currentItem.podcast?.audio?.duration
        if (duration != null)
            tvPodcastTime.text =
                CommonsKt.calculateDurationMinutesAndSeconds(duration.toLong())

        tvRecasts.text = currentItem.podcast?.number_of_recasts!!.toString()
        tvLikes.text = currentItem.podcast?.number_of_likes!!.toString()
        tvListens.text = currentItem.podcast?.number_of_listens!!.toString()
        tvComments.text = currentItem.podcast?.number_of_comments!!.toString()

        // user picture
        Glide.with(itemView.context)
            .load(currentItem.user.images.small_url)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)

        // main picture
        Glide.with(itemView.context)
            .load(currentItem.podcast?.images?.medium_url)
            .into(ivMainFeedPicture)

        // verified
        if (currentItem.user.verified)
            ivVerifiedUser.visibility = View.VISIBLE
        else
            ivVerifiedUser.visibility = View.GONE

        // like
        currentItem.podcast?.liked?.let {
            if (it)
                ibtnLike.setImageResource(R.drawable.like_filled)
            else
                ibtnLike.setImageResource(R.drawable.like)
        } ?: run {
            ibtnLike.setImageResource(R.drawable.like)
        }

        // recast
        currentItem.podcast?.recasted?.let {
            if (it) {
                ibtnRecasts.setImageResource(R.drawable.recast_filled)
                tvSomeoneRecasted.visibility = View.VISIBLE
            } else {
                ibtnRecasts.setImageResource(R.drawable.recast)
                tvSomeoneRecasted.visibility = View.GONE
            }


        } ?: run {
            ibtnRecasts.setImageResource(R.drawable.recast)
        }
    }


    private fun fillCaption(caption: String?): SpannableString? {
        val hashtaggedString = SpannableString(caption)

        caption?.let {
            val regex = "#[\\w]+"

            val pattern: Pattern = Pattern.compile(regex, Pattern.MULTILINE)
            val matcher: Matcher = pattern.matcher(caption)

            while (matcher.find()) {
                val textFound = matcher.group(0)
                val startIndex = matcher.start(0)
                val endIndex = matcher.end(0)
                hashtaggedString.setSpan(clickableSpan, startIndex, endIndex, 0)
                println("Hemos encontrado el texto $textFound que empieza en $startIndex y acaba en $endIndex")
            }

        }
        return hashtaggedString
    }

}