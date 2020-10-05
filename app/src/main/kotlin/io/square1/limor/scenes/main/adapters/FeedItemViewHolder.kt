package io.square1.limor.scenes.main.adapters

import android.content.Context
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
import io.square1.limor.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.lang.Exception
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class FeedItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val feedClickListener: FeedAdapter.OnFeedClickListener,
    private val userLogged: UIUser?,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_feed_item_recycler_view,
        parent,
        false
    )
) {
    private var tvUserFullname: TextView = itemView.findViewById(R.id.tvUserName)
    private var tvDateAndLocation: TextView = itemView.findViewById(R.id.tvTimeAndLocation)
    private var tvPodcastTitle: TextView = itemView.findViewById(R.id.tvPodcastTitle)
    private var tvPodcastText: TextView = itemView.findViewById(R.id.tvPodcastText)
    private var tvPodcastTime: TextView = itemView.findViewById(R.id.tvPodcastTime)
    private var tvSomeoneRecasted: TextView = itemView.findViewById(R.id.tvSomeoneRecasted)

    private var tvListens: TextView = itemView.findViewById(R.id.tvListens)
    private var tvComments: TextView = itemView.findViewById(R.id.tvComments)
    private var tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
    private var tvRecasts: TextView = itemView.findViewById(R.id.tvRecasts)

    private var ivUser: ImageView = itemView.findViewById(R.id.ivUserPicture)
    private var ivMainFeedPicture: ImageView = itemView.findViewById(R.id.ivMainFeedPicture)
    private var ivVerifiedUser: ImageView = itemView.findViewById(R.id.ivVerifiedUser)

    private var ibtnListen: ImageButton = itemView.findViewById(R.id.btnListens)
    private var ibtnLike: ImageButton = itemView.findViewById(R.id.btnLikes)
    private var ibtnRecasts: ImageButton = itemView.findViewById(R.id.btnRecasts)
    private var ibtnComments: ImageButton = itemView.findViewById(R.id.btnComments)
    private var ibtnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    private var ibtnSend: ImageButton = itemView.findViewById(R.id.btnSend)
    private var ibtnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)


    fun bind(currentItem: UIFeedItem, position: Int, showPlayButton: Boolean) {
        // fullname
        var firstName = ""
        currentItem.podcast?.user?.first_name?.let { firstName = it }
        var lastName = ""
        currentItem.podcast?.user?.last_name?.let { lastName = it }
        val fullname = "$firstName $lastName"
        tvUserFullname.text = fullname
        tvUserFullname.onClick { feedClickListener.onUserClicked(currentItem, position) }


        // datetime and location
        val lat = currentItem.podcast?.latitude
        val lng = currentItem.podcast?.longitude
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
        val dateAndLocationString = "$datetimeString$locationString"
        tvDateAndLocation.text = dateAndLocationString

        // title & caption
        currentItem.podcast?.title?.let { tvPodcastTitle.text = it }
        currentItem.podcast?.caption?.let { tvPodcastText.text = hightlightHashtags(it) }
        tvPodcastText.movementMethod = LinkMovementMethod.getInstance()

        // duration
//        currentItem.podcast?.audio?.duration?.let {
        currentItem.podcast?.audio?.total_length?.let {
            tvPodcastTime.text = CommonsKt.calculateDurationMinutesAndSeconds(it.toLong())
        }

        // recasts
        currentItem.podcast?.number_of_recasts?.let { tvRecasts.text = it.toString() }
        tvRecasts.onClick { feedClickListener.onRecastClicked(currentItem, position) }
        ibtnRecasts.onClick { feedClickListener.onRecastClicked(currentItem, position) }

        // likes
        currentItem.podcast?.number_of_likes?.let { tvLikes.text = it.toString() }
        tvLikes.onClick {
            feedClickListener.onLikeClicked(currentItem, position)
        }
        ibtnLike.onClick {
            feedClickListener.onLikeClicked(currentItem, position)
        }

        // listens
        currentItem.podcast?.number_of_listens?.let { tvListens.text = it.toString() }
        tvListens.onClick { feedClickListener.onListenClicked(currentItem, position) }
        ibtnListen.onClick { feedClickListener.onListenClicked(currentItem, position) }

        // comments
        currentItem.podcast?.number_of_comments?.let { tvComments.text = it.toString() }
        tvComments.onClick { feedClickListener.onCommentClicked(currentItem, position) }
        ibtnComments.onClick { feedClickListener.onCommentClicked(currentItem, position) }

        // user picture
        Glide.with(itemView.context)
            .load(currentItem.podcast?.user?.images?.small_url)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)
        ivUser.onClick { feedClickListener.onUserClicked(currentItem, position) }

        // main picture
        Glide.with(itemView.context)
            .load(currentItem.podcast?.images?.medium_url)
            .into(ivMainFeedPicture)
        ivMainFeedPicture.onClick { feedClickListener.onItemClicked(currentItem, position) }

        // verified
        currentItem.podcast?.user?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
            else
                ivVerifiedUser.visibility = View.GONE
        } ?: run {
            ivVerifiedUser.visibility = View.GONE
        }

        // like
        currentItem.podcast?.liked?.let {
            if (it)
                ibtnLike.setImageResource(R.drawable.like_filled)
            else
                ibtnLike.setImageResource(R.drawable.like)
        } ?: run {
            ibtnLike.setImageResource(R.drawable.like)
        }

        // my own recast
        currentItem.podcast?.recasted?.let {
            if(it) {
                ibtnRecasts.setImageResource(R.drawable.recast_filled)
            } else {
                ibtnRecasts.setImageResource(R.drawable.recast)
            }
        } ?: run {
            ibtnRecasts.setImageResource(R.drawable.recast)
        }

        // someone recasted
        currentItem.recasted.let {
            if (it) {
                val userRecasted = currentItem.user
                userLogged?.id?.let { userLoggedId ->
                    tvSomeoneRecasted.visibility = View.VISIBLE

                    val fullnameRecasted = if (userRecasted.id == userLoggedId)
                        context.getString(R.string.you)
                    else
                        userRecasted.first_name + " " + userRecasted.last_name

                    tvSomeoneRecasted.text = String.format(
                        context.resources.getString(R.string.someone_recasted), fullnameRecasted
                    )
                }

            } else {
                tvSomeoneRecasted.visibility = View.GONE
            }

        } ?: run {
            tvSomeoneRecasted.visibility = View.GONE
        }


        ibtnMore.onClick { feedClickListener.onMoreClicked(currentItem, position, ibtnMore) }
        ibtnSend.onClick { feedClickListener.onSendClicked(currentItem, position) }

        if(showPlayButton){
            ibtnPlay.onClick { feedClickListener.onPlayClicked(currentItem, position) }
        }else{
            ibtnPlay.visibility = View.GONE
        }
    }


    private fun hightlightHashtags(caption: String?): SpannableString? {
        caption?.let {
            val hashtaggedString = SpannableString(caption)
            val regex = "#[\\w]+"

            val pattern: Pattern = Pattern.compile(regex, Pattern.MULTILINE)
            val matcher: Matcher = pattern.matcher(caption)

            while (matcher.find()) {
                val textFound = matcher.group(0)
                val startIndex = matcher.start(0)
                val endIndex = matcher.end(0)
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        val tv = textView as TextView
                        val s: Spanned = tv.text as Spanned
                        val start: Int = s.getSpanStart(this)
                        val end: Int = s.getSpanEnd(this)
                        val clickedTag = s.subSequence(start, end).toString()
                        feedClickListener.onHashtagClicked(clickedTag)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = true
                    }
                }
                hashtaggedString.setSpan(clickableSpan, startIndex, endIndex, 0)
                println("Hemos encontrado el texto $textFound que empieza en $startIndex y acaba en $endIndex")
            }
            return hashtaggedString
        }
        return SpannableString("")
    }

}