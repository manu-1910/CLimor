package io.square1.limor.scenes.main.fragments.podcast

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIFeedItem
import kotlinx.android.synthetic.main.include_podcast_data.*
import kotlinx.android.synthetic.main.toolbar_with_logo_and_back_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.lang.Exception
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class PodcastDetailsFragment : BaseFragment() {

    private var rootView: View? = null
    var app: App? = null

    var uiFeedItem: UIFeedItem? = null

    var clickableSpan: ClickableSpan =

        object : ClickableSpan() {
            override fun onClick(textView: View) {
                val tv = textView as TextView
                val s: Spanned = tv.text as Spanned
                val start: Int = s.getSpanStart(this)
                val end: Int = s.getSpanEnd(this)
                val clickedTag = s.subSequence(start, end).toString()
                onHashtagClicked(clickedTag)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }




    companion object {
        val TAG: String = PodcastDetailsFragment::class.java.simpleName
        fun newInstance() = PodcastDetailsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_podcast_details, container, false)

        }
        app = context?.applicationContext as App
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTranslationZ(view, 1f)

        val activity = activity as PodcastDetailsActivity?
        uiFeedItem = activity?.uiFeedItem

        configureToolbar()
        fillForm()
    }

    private fun fillForm() {
        tvPodcastText?.text = uiFeedItem?.podcast?.caption
        tvPodcastTitle?.text = uiFeedItem?.podcast?.title
        val fullName =
            uiFeedItem?.podcast?.user?.first_name + " " + uiFeedItem?.podcast?.user?.last_name
        tvUserName?.text = fullName
        tvUserName.onClick { onUserClicked() }


        // datetime and location
        val lat = uiFeedItem?.podcast?.latitude
        val lng = uiFeedItem?.podcast?.longitude
        var locationString = ""
        if (lat != null && lng != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
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


        var datetimeString = ""
        uiFeedItem?.created_at?.let {
            datetimeString = CommonsKt.getDateTimeFormattedFromTimestamp(it.toLong())
        }
        val dateAndLocationString = "$datetimeString$locationString"
        tvTimeAndLocation?.text = dateAndLocationString

        // title & caption
        uiFeedItem?.podcast?.title?.let { tvPodcastTitle.text = it }
        uiFeedItem?.podcast?.caption?.let { tvPodcastText.text = hightlightHashtags(it) }
        tvPodcastText?.movementMethod = LinkMovementMethod.getInstance()

        // duration
        uiFeedItem?.podcast?.audio?.duration?.let {
            tvPodcastTime?.text = CommonsKt.calculateDurationMinutesAndSeconds(it.toLong())
        }

        // recasts
        uiFeedItem?.podcast?.number_of_recasts?.let { tvRecasts.text = it.toString() }
        tvRecasts?.onClick { onRecastClicked() }
        btnRecasts?.onClick { onRecastClicked() }

        // likes
        uiFeedItem?.podcast?.number_of_likes?.let { tvLikes.text = it.toString() }
        tvLikes?.onClick { onLikeClicked() }
        btnLikes?.onClick { onLikeClicked() }

        // listens
        uiFeedItem?.podcast?.number_of_listens?.let { tvListens.text = it.toString() }
        tvListens?.onClick { onListensClicked() }
        btnListens?.onClick { onListensClicked() }

        // comments
        uiFeedItem?.podcast?.number_of_comments?.let { tvComments.text = it.toString() }
        tvComments?.onClick { onCommentsClicked() }
        btnComments?.onClick { onCommentsClicked() }

        context?.let {
            // user picture
            Glide.with(it)
                .load(uiFeedItem?.user?.images?.small_url)
                .apply(RequestOptions.circleCropTransform())
                .into(ivUserPicture)
            ivUserPicture?.onClick { onUserClicked() }

            // main picture
            Glide.with(it)
                .load(uiFeedItem?.podcast?.images?.medium_url)
                .into(ivMainFeedPicture)
            ivMainFeedPicture.onClick { onItemClicked() }
        }


        // verified
        uiFeedItem?.user?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
            else
                ivVerifiedUser.visibility = View.GONE
        } ?: run {
            ivVerifiedUser.visibility = View.GONE
        }


        // like
        uiFeedItem?.podcast?.liked?.let {
            if (it)
                btnLikes?.setImageResource(R.drawable.like_filled)
            else
                btnLikes?.setImageResource(R.drawable.like)
        } ?: run {
            btnLikes?.setImageResource(R.drawable.like)
        }

        // recast
        uiFeedItem?.podcast?.recasted?.let {
            if (it) {
                btnRecasts?.setImageResource(R.drawable.recast_filled)
                tvSomeoneRecasted.visibility = View.VISIBLE
            } else {
                btnRecasts?.setImageResource(R.drawable.recast)
                tvSomeoneRecasted.visibility = View.GONE
            }


        } ?: run {
            btnRecasts?.setImageResource(R.drawable.recast)
        }


        btnMore?.onClick { onMoreClicked() }
        btnSend?.onClick { onSendClicked() }
        btnPlay?.onClick { onPlayClicked() }
    }

    private fun onPlayClicked() {
        Toast.makeText(context, "Play clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onSendClicked() {
        Toast.makeText(context, "Send clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onMoreClicked() {
        Toast.makeText(context, "More clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onItemClicked() {
        Toast.makeText(context, "Item clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onCommentsClicked() {
        Toast.makeText(context, "Comments clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onListensClicked() {
        Toast.makeText(context, "Listens clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onLikeClicked() {
        Toast.makeText(context, "Likes clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onRecastClicked() {
        Toast.makeText(context, "Recasts clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onUserClicked() {
        Toast.makeText(context, "User clicked", Toast.LENGTH_SHORT).show()
    }

    private fun configureToolbar() {
        btnClose?.onClick { activity?.finish() }
    }

    private fun onHashtagClicked(clickedTag: String) {
        Toast.makeText(context, "User clicked hashtag $clickedTag", Toast.LENGTH_SHORT).show()
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
                hashtaggedString.setSpan(clickableSpan, startIndex, endIndex, 0)
                println("Hemos encontrado el texto $textFound que empieza en $startIndex y acaba en $endIndex")
            }
            return hashtaggedString
        }
        return SpannableString("")
    }


}