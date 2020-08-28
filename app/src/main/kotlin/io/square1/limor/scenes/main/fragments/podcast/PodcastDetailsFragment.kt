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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.adapters.CommentsAdapter
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UIComment
import io.square1.limor.uimodels.UIFeedItem
import io.square1.limor.uimodels.UIPodcast
import kotlinx.android.synthetic.main.fragment_feed.*
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


data class CommentWithParent(val comment : UIComment, val parent : UIComment?)



class PodcastDetailsFragment : BaseFragment() {

    private var lastCommentRequestedRepliesPosition: Int = 0
    private var lastCommentRequestedRepliesParent: UIComment? = null
    private var lastLikedItemPosition = 0
    private var isScrolling = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelGetPodcastComments: GetPodcastCommentsViewModel
    private lateinit var viewModelGetCommentComments: GetCommentCommentsViewModel
    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel
    private lateinit var viewModelDeletePodcastLike: DeletePodcastLikeViewModel
    private lateinit var viewModelCreateCommentLike: CreateCommentLikeViewModel
    private lateinit var viewModelDeleteCommentLike: DeleteCommentLikeViewModel
    private val getPodcastCommentsDataTrigger = PublishSubject.create<Unit>()
    private val getCommentCommentsDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val createCommentLikeDataTrigger = PublishSubject.create<Unit>()
    private val deleteCommentLikeDataTrigger = PublishSubject.create<Unit>()

    private val commentWithParentsItemsList = ArrayList<CommentWithParent>()

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

    private var commentsAdapter: CommentsAdapter? = null


    companion object {
        val TAG: String = PodcastDetailsFragment::class.java.simpleName
        fun newInstance() = PodcastDetailsFragment()
        private const val OFFSET_INFINITE_SCROLL = 2
        private const val FEED_LIMIT_REQUEST = 10
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

        bindViewModel()
        initApiCallGetPodcastComments()
        initApiCallGetCommentComments()
        initApiCallCreatePodcastLike()
        initApiCallDeletePodcastLike()
        initApiCallCreateCommentLike()
        initApiCallDeleteCommentLike()
        configureAdapter()
        configureToolbar()
        fillForm()

        uiFeedItem?.podcast?.id?.let { viewModelGetPodcastComments.idPodcast = it }
        getPodcastCommentsDataTrigger.onNext(Unit)

        swipeRefreshLayout?.onRefresh { reloadComments() }
    }

    private fun initApiCallGetCommentComments() {
        val output = viewModelGetCommentComments.transform(
            GetCommentCommentsViewModel.Input(
                getCommentCommentsDataTrigger
            )
        )

        output.response.observe(this, Observer {
            val newItems = it.data.comments

            // we add the new items to its parent
            lastCommentRequestedRepliesParent?.let {parent ->
                parent.comments.addAll(newItems)

                // now we have to insert the new items in the global list
                fillCommentsWithParentsListOneLevel(lastCommentRequestedRepliesPosition, newItems, parent)
            }



            rvComments?.adapter?.notifyDataSetChanged()
            hideSwipeToRefreshProgressBar()
        })

        output.errorMessage.observe(this, Observer {
            hideSwipeToRefreshProgressBar()
            Toast.makeText(
                context,
                "We couldn't get your feed, please, try again later",
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun fillCommentsWithParentsListOneLevel(position: Int, newItems: ArrayList<UIComment>, parent : UIComment) {
        for (i in 0 until newItems.size) {
            commentWithParentsItemsList.add(position + i + 1, CommentWithParent(newItems[i], parent))
        }
    }

    private fun initApiCallCreateCommentLike() {
        val output = viewModelCreateCommentLike.transform(
            CreateCommentLikeViewModel.Input(
                createCommentLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoCommentLike()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoCommentLike()
        })
    }

    private fun initApiCallDeletePodcastLike() {
        val output = viewModelDeletePodcastLike.transform(
            DeletePodcastLikeViewModel.Input(
                deletePodcastLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoPodcastLike()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoPodcastLike()
        })
    }

    private fun initApiCallCreatePodcastLike() {
        val output = viewModelCreatePodcastLike.transform(
            CreatePodcastLikeViewModel.Input(
                createPodcastLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoPodcastLike()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoPodcastLike()
        })
    }

    private fun undoPodcastLike() {
        Toast.makeText(context, getString(R.string.error_liking_podcast), Toast.LENGTH_SHORT).show()
        uiFeedItem?.podcast?.let { podcast -> changeItemLikeStatus(podcast, !podcast.liked) }
    }

    private fun initApiCallDeleteCommentLike() {
        val output = viewModelDeleteCommentLike.transform(
            DeleteCommentLikeViewModel.Input(
                deleteCommentLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoCommentLike()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoCommentLike()
        })
    }

    private fun undoCommentLike() {
        Toast.makeText(context, getString(R.string.error_liking_podcast), Toast.LENGTH_SHORT).show()
        val item = commentWithParentsItemsList[lastLikedItemPosition]
        item.comment.podcast?.let { podcast ->
            changeItemLikeStatus(
                item.comment,
                lastLikedItemPosition,
                !podcast.liked
            )
        }
    }


    private fun initApiCallGetPodcastComments() {
        val output = viewModelGetPodcastComments.transform(
            GetPodcastCommentsViewModel.Input(
                getPodcastCommentsDataTrigger
            )
        )

        output.response.observe(this, Observer {
            val newItems = it.data.comments
            if (newItems.size == 0)
                isLastPage = true

            if (isReloading) {
                commentWithParentsItemsList.clear()
                rvComments?.recycledViewPool?.clear()
                isReloading = false
            }

            fillCommentsWithParentsListOneLevel(newItems)

            rvComments?.adapter?.notifyDataSetChanged()
            hideSwipeToRefreshProgressBar()
        })

        output.errorMessage.observe(this, Observer {
            hideSwipeToRefreshProgressBar()
            Toast.makeText(
                context,
                "We couldn't get your feed, please, try again later",
                Toast.LENGTH_SHORT
            ).show()
        })
    }


    private fun fillCommentsWithParentsListAllLevels(newItems: ArrayList<UIComment>, parent: UIComment?) {
        newItems.forEach { comment ->
            commentWithParentsItemsList.add(CommentWithParent(comment, null))

            if (comment.comments.size > 0) {
                comment.comments.forEach{subComment ->
                    fillCommentsWithParentsListAllLevels(comment.comments, comment)
                }
            }
        }
    }


    private fun countAllComments(commentList: ArrayList<UIComment>): Int {
        var count = 0
        commentList.forEach { currentComment ->
            if( currentComment.comments.size == 0) {
                count++
            } else {
                count += countAllComments(currentComment.comments) + 1
            }
        }
        return count
    }

    private fun fillCommentsWithParentsListOneLevel(newItems: ArrayList<UIComment>) {
        newItems.forEach { comment ->
            commentWithParentsItemsList.add(CommentWithParent(comment, null))

            if (comment.comments.size > 0) {
                comment.comments.forEach{subComment ->
                    commentWithParentsItemsList.add(CommentWithParent(subComment, comment))
                }
            }
        }
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
                commentWithParentsItemsList,
                uiFeedItem!!.podcast!!,
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
                        changeItemLikeStatus(
                            item,
                            position,
                            !item.liked
                        ) // careful, it will change an item to be from like to dislike and viceversa
                        lastLikedItemPosition = position

                        // if now it's liked, let's call the api
                        if (item.liked) {
                            viewModelCreateCommentLike.idComment = item.id
                            createCommentLikeDataTrigger.onNext(Unit)

                            // if now it's not liked, let's call the api
                        } else {
                            viewModelDeleteCommentLike.idComment = item.id
                            deleteCommentLikeDataTrigger.onNext(Unit)
                        }
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

                    override fun onMoreRepliesClicked(parent: UIComment, position: Int) {
                        lastCommentRequestedRepliesPosition = position
                        lastCommentRequestedRepliesParent = parent
                        viewModelGetCommentComments.idComment = parent.id
                        viewModelGetCommentComments.offset = parent.comments.size
                        getCommentCommentsDataTrigger.onNext(Unit)
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
                // if we scroll down...
                if (dy > 0) {

                    // those are the items that we have already passed in the list, the items we already saw
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    // this are the items that are currently showing on screen
                    val visibleItemsCount = layoutManager.childCount

                    // this are the total amount of items
                    val totalItemsCount = layoutManager.itemCount

                    // if the past items + the current visible items + offset is greater than the total amount of items, we have to retrieve more data
                    if (isScrolling && !isLastPage && visibleItemsCount + pastVisibleItems + OFFSET_INFINITE_SCROLL >= totalItemsCount) {
                        isScrolling = false
                        viewModelGetPodcastComments.offset = commentWithParentsItemsList.size - 1
                        getPodcastCommentsDataTrigger.onNext(Unit)
                    }
                }
            }
        })
        rvComments?.isNestedScrollingEnabled = true
        rvComments?.setHasFixedSize(true)
    }


    private fun changeItemLikeStatus(item: UIComment, position: Int, liked: Boolean) {
        if (liked) {
            item.number_of_likes++
        } else {
            item.number_of_likes--
        }
        item.liked = liked
        commentsAdapter?.notifyItemChanged(position, item)
    }

    private fun changeItemLikeStatus(podcast: UIPodcast, liked: Boolean) {
        if (liked) {
            podcast.number_of_likes++
        } else {
            podcast.number_of_likes--
        }
        podcast.liked = liked
        fillFormLikePodcastData()
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelGetPodcastComments = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetPodcastCommentsViewModel::class.java)
        }
        viewModelGetPodcastComments.limit = FEED_LIMIT_REQUEST
        viewModelGetPodcastComments.offset = 0

        activity?.let { fragmentActivity ->
            viewModelGetCommentComments = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetCommentCommentsViewModel::class.java)
        }
        viewModelGetCommentComments.limit = FEED_LIMIT_REQUEST
        viewModelGetCommentComments.offset = 0


        activity?.let { fragmentActivity ->
            viewModelCreatePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastLikeViewModel::class.java)
        }
        uiFeedItem?.podcast?.id?.let { viewModelCreatePodcastLike.idPodcast = it }


        activity?.let { fragmentActivity ->
            viewModelDeletePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastLikeViewModel::class.java)
        }
        uiFeedItem?.podcast?.id?.let { viewModelDeletePodcastLike.idPodcast = it }

        activity?.let { fragmentActivity ->
            viewModelCreateCommentLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateCommentLikeViewModel::class.java)
        }

        activity?.let { fragmentActivity ->
            viewModelDeleteCommentLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeleteCommentLikeViewModel::class.java)
        }
    }

    private fun reloadComments() {
        isLastPage = false
        isReloading = true
        viewModelGetPodcastComments.offset = 0
        getPodcastCommentsDataTrigger.onNext(Unit)
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
        tvLikes?.onClick { onPodcastLikeClicked() }
        btnLikes?.onClick { onPodcastLikeClicked() }

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

        fillFormLikePodcastData()
    }

    private fun fillFormLikePodcastData() {
        uiFeedItem?.podcast?.number_of_likes?.let { tvLikes.text = it.toString() }
        uiFeedItem?.podcast?.liked?.let {
            if (it)
                btnLikes?.setImageResource(R.drawable.like_filled)
            else
                btnLikes?.setImageResource(R.drawable.like)
        } ?: run {
            btnLikes?.setImageResource(R.drawable.like)
        }
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

    private fun onPodcastLikeClicked() {
        uiFeedItem?.podcast?.let { podcast ->
            changeItemLikeStatus(
                podcast,
                !podcast.liked
            ) // careful, it will change an item to be from like to dislike and viceversa


            // if now it's liked, let's call the api
            if (podcast.liked) {
                viewModelCreatePodcastLike.idPodcast = podcast.id
                createPodcastLikeDataTrigger.onNext(Unit)

                // if now it's not liked, let's call the api
            } else {
                viewModelDeletePodcastLike.idPodcast = podcast.id
                deletePodcastLikeDataTrigger.onNext(Unit)
            }
        }


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