package io.square1.limor.scenes.main.fragments.podcast

import android.content.Intent
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
import android.widget.AbsListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.adapters.CommentsAdapter
import io.square1.limor.scenes.main.viewmodels.GetCommentsViewModel
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIComment
import io.square1.limor.uimodels.UIFeedItem
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import kotlinx.android.synthetic.main.include_interactions_bar.*
import kotlinx.android.synthetic.main.include_podcast_data.*
import kotlinx.android.synthetic.main.include_user_bar.*
import kotlinx.android.synthetic.main.toolbar_with_logo_and_back_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.onRefresh
import timber.log.Timber
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList

class PodcastDetailsFragment : BaseFragment() {

    private var totalItems = 0
    private var currentItems = 0
    private var scrollOutItem = 0
    private var isScrolling = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelGetComments: GetCommentsViewModel
    private val getCommentsDataTrigger = PublishSubject.create<Unit>()

    private val commentItemsList = ArrayList<UIComment>()

    private val FEED_LIMIT_REQUEST = 10
    private var isLastPage = false
    private var rootView: View? = null
    var app: App? = null

    var uiFeedItem: UIFeedItem? = null

    var clickableSpan: ClickableSpan = object : ClickableSpan() {
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

    private var isReloading = false

    private var commentsAdapter : CommentsAdapter? = null


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

    private fun initApiCallGetComments() {
        val output = viewModelGetComments.transform(
            GetCommentsViewModel.Input(
                getCommentsDataTrigger
            )
        )

        output.response.observe(this, Observer {
//            System.out.println("Hemos obtenido datos del feeddddddd")
//            System.out.println("Hemos obtenido ${it.data.feed_items.size} items")
            val newItems = it.data.comments

            if (isReloading) {
                commentItemsList.clear()
                rvComments?.recycledViewPool?.clear()
                isReloading = false
            }

            commentItemsList.addAll(newItems)
            if (newItems.size < FEED_LIMIT_REQUEST)
                isLastPage = true


            rvComments?.adapter?.notifyDataSetChanged()
            hideSwipeToRefreshProgressBar()
        })

        output.errorMessage.observe(this, Observer {
            hideSwipeToRefreshProgressBar()
            Toast.makeText(context, "We couldn't get your feed, please, try again later", Toast.LENGTH_SHORT).show()
        })
    }

    private fun hideSwipeToRefreshProgressBar() {
        swipeRefreshLayout?.let {
            if (it.isRefreshing) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }
    }


    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvComments?.layoutManager = layoutManager
        commentsAdapter = context?.let {
            CommentsAdapter(
                it,
                commentItemsList,
                object : CommentsAdapter.OnCommentClickListener {
                    override fun onItemClicked(item: UIComment, position: Int) {
//                        val podcastDetailsIntent = Intent(context, PodcastDetailsActivity::class.java)
//                        podcastDetailsIntent.putExtra("model", item)
//                        startActivity(podcastDetailsIntent)
//                        (activity as SignActivity).finish()
                        Toast.makeText(context, "You clicked on item", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPlayClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on play", Toast.LENGTH_SHORT).show()
                    }

                    override fun onListenClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on listen", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCommentClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on comment", Toast.LENGTH_SHORT).show()
                    }

                    override fun onLikeClicked(item: UIComment, position: Int) {
//                        item.podcast?.let { podcast ->
//                            changeItemLikeStatus(item, position, !podcast.liked) // careful, it will change an item to be from like to dislike and viceversa
//                            lastLikedItemPosition = position
//
//                            // if now it's liked, let's call the api
//                            if(podcast.liked) {
//                                viewModelCreatePodcastLike.idPodcast = podcast.id
//                                createPodcastLikeDataTrigger.onNext(Unit)
//
//                                // if now it's not liked, let's call the api
//                            } else {
//                                viewModelDeletePodcastLike.idPodcast = podcast.id
//                                deletePodcastLikeDataTrigger.onNext(Unit)
//                            }
//                        }
                        Toast.makeText(context, "You clicked on like", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRecastClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on recast", Toast.LENGTH_SHORT).show()
                    }

                    override fun onHashtagClicked(hashtag: String) {
                        Toast.makeText(
                            context,
                            "You clicked on $hashtag hashtag",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onSendClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on share", Toast.LENGTH_SHORT).show()
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }

                    override fun onUserClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on user", Toast.LENGTH_SHORT).show()
                    }

                    override fun onMoreClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on more", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReplyClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on reply", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        rvComments?.adapter = commentsAdapter
        rvComments?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = layoutManager.childCount
                totalItems = layoutManager.itemCount

                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItem >= 0) {
                    scrollOutItem = firstVisibleItem
                }

                if (!isLastPage)
                    if (isScrolling && currentItems + scrollOutItem == totalItems) {
                        isScrolling = false
                        // we have to recall the api to get new values
                        viewModelGetComments.offset = commentItemsList.size - 1
                        getCommentsDataTrigger.onNext(Unit)
                    }
            }
        })
        rvComments?.isNestedScrollingEnabled = true
        rvComments?.setHasFixedSize(false)
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.getDrawable(R.drawable.divider_item_recyclerview)?.let { divider.setDrawable(it) }
        rvComments?.addItemDecoration(divider)

    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelGetComments = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetCommentsViewModel::class.java)
        }
        viewModelGetComments.limit = FEED_LIMIT_REQUEST
        viewModelGetComments.offset = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTranslationZ(view, 1f)

        val activity = activity as PodcastDetailsActivity?
        uiFeedItem = activity?.uiFeedItem

        bindViewModel()
        initApiCallGetComments()
        configureAdapter()
        configureToolbar()
        fillForm()

        uiFeedItem?.podcast?.id?.let { viewModelGetComments.idPodcast = it }
        getCommentsDataTrigger.onNext(Unit)

        swipeRefreshLayout?.onRefresh { reloadComments() }
    }

    private fun reloadComments() {
        isLastPage = false
        isReloading = true
        viewModelGetComments.offset = 0
        getCommentsDataTrigger.onNext(Unit)
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
                .load(uiFeedItem?.podcast?.user?.images?.small_url)
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


    // region listeners
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
    // endregion listeners


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