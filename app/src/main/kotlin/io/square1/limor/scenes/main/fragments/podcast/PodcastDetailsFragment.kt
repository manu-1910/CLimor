package io.square1.limor.scenes.main.fragments.podcast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.Constants
import io.square1.limor.common.SessionManager
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.extensions.showKeyboard
import io.square1.limor.scenes.main.adapters.CommentsAdapter
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.service.AudioService
import io.square1.limor.uimodels.UIComment
import io.square1.limor.uimodels.UICommentRequest
import io.square1.limor.uimodels.UICreateCommentRequest
import io.square1.limor.uimodels.UIPodcast
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import kotlinx.android.synthetic.main.include_interactions_bar.*
import kotlinx.android.synthetic.main.include_podcast_data.*
import kotlinx.android.synthetic.main.include_user_bar.*
import kotlinx.android.synthetic.main.toolbar_with_logo_and_back_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.io.File
import java.io.Serializable
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList


data class CommentWithParent(val comment: UIComment, val parent: CommentWithParent?) : Serializable {
    fun getParentCount() : Int {
        var parentCount = 0
        var auxComment = parent
        while (auxComment != null) {
            parentCount++
            auxComment = auxComment.parent
        }
        return parentCount
    }
}


class PodcastDetailsFragment : BaseFragment() {

    private var currentCommentRecorded: File? = null
    private var isRecording = false
    private val handlerRecordingComment = Handler()
    private lateinit var updater: Runnable

    private lateinit var mRecorder: SimpleRecorder
    private var lastCommentRequestedRepliesPosition: Int = 0
    private var lastCommentRequestedRepliesParent: CommentWithParent? = null
    private var lastLikedItemPosition = 0
    private var isScrolling = false

    // this variable will be true when we are showing the details of a podcast with the podcast comments
    // it will be false when we are showing the details of a podcast but with the comments of a comment and its parents
    private var podcastMode = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelGetPodcastComments: GetPodcastCommentsViewModel
    private lateinit var viewModelGetCommentComments: GetCommentCommentsViewModel
    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel
    private lateinit var viewModelDeletePodcastLike: DeletePodcastLikeViewModel
    private lateinit var viewModelCreatePodcastRecast: CreatePodcastRecastViewModel
    private lateinit var viewModelDeletePodcastRecast: DeletePodcastRecastViewModel
    private lateinit var viewModelCreateCommentLike: CreateCommentLikeViewModel
    private lateinit var viewModelDeleteCommentLike: DeleteCommentLikeViewModel
    private lateinit var viewModelCreatePodcastComment: CreatePodcastCommentViewModel
    private lateinit var viewModelCreateCommentComment: CreateCommentCommentViewModel
    private val getPodcastCommentsDataTrigger = PublishSubject.create<Unit>()
    private val getCommentCommentsDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastRecastDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastRecastDataTrigger = PublishSubject.create<Unit>()
    private val createCommentLikeDataTrigger = PublishSubject.create<Unit>()
    private val deleteCommentLikeDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastCommentDataTrigger = PublishSubject.create<Unit>()
    private val createCommentCommentDataTrigger = PublishSubject.create<Unit>()

    private val commentWithParentsItemsList = ArrayList<CommentWithParent>()

    private var isLastPage = false
    private var rootView: View? = null
    var app: App? = null

    private var firstTimePadding = true

    // this represents the main podcast of the screen, the postcast that we are seeing the details of
    var uiPodcast: UIPodcast? = null

    // this represents the main comment of the screen, it will be null when we are in podcastMode
    // and it will be the main comment when we are seeing the comments of this comment
    private var uiMainCommentWithParent: CommentWithParent? = null



    @Inject
    lateinit var sessionManager : SessionManager



    private var isReloading = false

    private var commentsAdapter: CommentsAdapter? = null


    companion object {
        val TAG: String = PodcastDetailsFragment::class.java.simpleName
        fun newInstance() = PodcastDetailsFragment()
        private const val OFFSET_INFINITE_SCROLL = 2
        private const val FEED_LIMIT_REQUEST = 10
        private val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val PERMISSION_ALL = 1
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
        // we get the main podcast we receive from the previous activity. It should be non null always
        uiPodcast = activity?.uiPodcast


        bindViewModel()
        initEmptyScenario()
        showEmptyScenario()
        initApiCallGetPodcastComments()
        initApiCallGetCommentComments()
        initApiCallCreatePodcastLike()
        initApiCallDeletePodcastLike()
        initApiCallCreatePodcastRecast()
        initApiCallDeletePodcastRecast()
        initApiCallCreateCommentLike()
        initApiCallDeleteCommentLike()
        initApiCallCreateComment()
        configureToolbar()

        // we get the possible comment clicked from the previous activity.
        uiMainCommentWithParent = activity?.commentWithParent
        configureAdapter()

        showProgressBar()

        initPodcastOrCommentMode()

        fillForm()
        initListeners()
        activity?.startCommenting?.let {
            if(it)
                openCommentBarTextAndFocusIt()
        }
    }

    private fun initPodcastOrCommentMode() {
        // if it's not null, that means that we have to show the "comment of a comment" screen.
        // If it's null this means that we have to show the "comment of a podcast" screen
        uiMainCommentWithParent?.let {
            podcastMode = false
            commentsAdapter?.podcastMode = podcastMode
            hideEmptyScenario()
            addTopParents(it)
            commentsAdapter?.mainCommentPosition = commentWithParentsItemsList.indexOf(it)


            // this is the code that makes the mainComment be on top of the scroll
            rvComments.viewTreeObserver.addOnGlobalLayoutListener {
                if (firstTimePadding) {
                    firstTimePadding = false
                    context?.let { context ->
                        rvComments?.setPadding(
                            0,
                            0,
                            0,
                            layParent!!.height - CommonsKt.dpToPx(128.0f, context)
                        )
                    } // TODO Jose, this last param should be the height of the last item, not hardcoded number
                    rvComments?.requestLayout()
                    val y: Float = rvComments!!.y
                    rvComments?.startNestedScroll(
                        ViewCompat.SCROLL_AXIS_VERTICAL,
                        ViewCompat.TYPE_NON_TOUCH
                    )
                    val llm = rvComments?.layoutManager as LinearLayoutManager
                    llm.scrollToPositionWithOffset(commentWithParentsItemsList.size - 1, y.toInt())
                }
            }

            app_bar_layout?.setExpanded(false)


            viewModelGetCommentComments.idComment = it.comment.id
            getCommentCommentsDataTrigger.onNext(Unit)

            rvComments?.adapter?.notifyDataSetChanged()

            // but if it is null, we have to get the podcast comments
        } ?: run {
            podcastMode = true
            commentsAdapter?.podcastMode = podcastMode
            uiPodcast?.id?.let { viewModelGetPodcastComments.idPodcast = it }
            getPodcastCommentsDataTrigger.onNext(Unit)
        }
    }

    private fun initApiCallDeletePodcastRecast() {
        val output = viewModelDeletePodcastRecast.transform(
            DeletePodcastRecastViewModel.Input(
                deletePodcastRecastDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoPodcastRecast()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoPodcastRecast()
        })
    }

    private fun undoPodcastRecast() {
        Toast.makeText(context, getString(R.string.error_liking_podcast), Toast.LENGTH_SHORT).show()
        uiPodcast?.let { podcast -> changeItemRecastStatus(podcast, !podcast.recasted) }
    }

    private fun changeItemRecastStatus(podcast: UIPodcast, recasted: Boolean) {
        if (recasted) {
            podcast.number_of_recasts++
        } else {
            podcast.number_of_recasts--
        }
        podcast.recasted = recasted
        fillFormRecastPodcastData()
    }

    private fun initApiCallCreatePodcastRecast() {
        val output = viewModelCreatePodcastRecast.transform(
            CreatePodcastRecastViewModel.Input(
                createPodcastRecastDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoPodcastRecast()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoPodcastRecast()
        })
    }

    private fun initEmptyScenario() {
        tvTitleEmptyScenario?.text = getString(R.string.no_comments_yet)
        tvCaptionEmptyScenario?.text = getString(R.string.when_comments_ready_will_appear_here)
    }

    private fun showEmptyScenario() {
//        layEmptyScenario?.visibility = View.VISIBLE
        app_bar_layout?.setExpanded(true)
        rvComments?.visibility = View.GONE

        val lp = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        collapsingToolbar.layoutParams = lp
    }

    private fun hideEmptyScenario() {
        layEmptyScenario?.visibility = View.GONE
        rvComments?.visibility = View.VISIBLE

        val lp = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        collapsingToolbar.layoutParams = lp
    }

    private fun initApiCallCreateComment() {
        val outputComment = viewModelCreateCommentComment.transform(
            CreateCommentCommentViewModel.Input(
                createCommentCommentDataTrigger
            )
        )

        val outputPodcast = viewModelCreatePodcastComment.transform(
            CreatePodcastCommentViewModel.Input(
                createPodcastCommentDataTrigger
            )
        )

        outputComment.response.observe(this, Observer {
            it.data?.comment?.let { newComment ->
                addNewCommentToList(newComment)
            }
            hideProgressBar()
            hideCommentBar()
        })

        outputPodcast.response.observe(this, Observer {
            it.data?.comment?.let { newComment ->
                addNewCommentToList(newComment)
            }
            hideProgressBar()
            hideCommentBar()
        })

        outputComment.errorMessage.observe(this, Observer {
            hideProgressBar()
            Toast.makeText(
                context,
                getString(R.string.couldnt_send_comment),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun hideCommentBar() {
        view?.hideKeyboard()
        commentBarUpperSide?.visibility = View.GONE
        etCommentUp?.setText("")
        etCommentDown?.visibility = View.VISIBLE
        visualizerComment?.visibility = View.GONE
    }


    private fun addNewCommentToList(commentCreated: UIComment) {
        if(podcastMode) {
            uiPodcast?.number_of_comments = uiPodcast?.number_of_comments!!.inc()
            tvComments.text = uiPodcast?.number_of_comments.toString()
            commentWithParentsItemsList.add(CommentWithParent(commentCreated, null))
        } else {
            uiPodcast?.number_of_comments = uiPodcast?.number_of_comments!!.inc()
            tvComments.text = uiPodcast?.number_of_comments.toString()

            // let's recalculate number of comments
            var parent = uiMainCommentWithParent?.parent
            while(parent != null) {
                parent.comment.comment_count = parent.comment.comment_count.inc()
                parent = parent.parent
            }
            uiMainCommentWithParent?.comment?.comment_count = uiMainCommentWithParent?.comment?.comment_count!!.inc()
            commentWithParentsItemsList.add(CommentWithParent(commentCreated, uiMainCommentWithParent))
        }
        app_bar_layout?.setExpanded(false)
        hideEmptyScenario()
        commentsAdapter?.notifyDataSetChanged()
        rvComments?.scrollToPosition(commentWithParentsItemsList.size - 1)
    }

    private fun initListeners() {
        etCommentDown.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    openCommentBarTextAndFocusIt()
                }
            }

        etCommentUp.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    commentBarUpperSide.visibility = View.GONE
                }
            }

        etCommentUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etCommentUp.text.isNotEmpty()) {
                    btnPost.setTextColor(ContextCompat.getColor(context!!, R.color.brandPrimary500))
                } else {
                    btnPost.setTextColor(ContextCompat.getColor(context!!, R.color.brandSecondary100))
                }
            }
        })

        btnPost.onClick {
            etCommentUp?.text?.let {
                if(it.isNotEmpty()) {
                    val request = UICreateCommentRequest(UICommentRequest(it.toString(), 0, null))
                    if(podcastMode) {
                        viewModelCreatePodcastComment.idPodcast = uiPodcast!!.id
                        viewModelCreatePodcastComment.uiCreateCommentRequest = request
                        createPodcastCommentDataTrigger.onNext(Unit)
                    } else {
                        viewModelCreateCommentComment.idComment = uiMainCommentWithParent!!.comment.id
                        viewModelCreateCommentComment.uiCreateCommentRequest = request
                        createCommentCommentDataTrigger.onNext(Unit)
                    }
                } else {
                    etCommentUp?.setError(getString(R.string.this_field_cant_be_empty))
                }
            }
        }

        btnTrash.onClick {
            currentCommentRecorded = null
            mRecorder.clear()
            visualizerComment.clear()
            visualizerComment.invalidate()
            btnRecord.setImageResource(R.drawable.record)
            btnPlayComment.visibility = View.GONE
            btnRecord.visibility = View.VISIBLE
            btnTrash.visibility = View.GONE
        }

        btnRecord.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if(!isCommentBarOpen())
                    openCommentBarTextAndFocusIt()

                btnRecord.setImageResource(R.drawable.record_red)
                audioSetup()
                recordAudio()
            } else if(event.action == MotionEvent.ACTION_UP) {
                stopAudio()
                btnRecord.setImageResource(R.drawable.record)
                view.performClick()
            }
            true
        }

        updater = object : Runnable {
            override fun run() {
                Timber.d("Inside updaterRunnable")
                handlerRecordingComment.postDelayed(this, 150)
                if(isRecording) {
                    val maxAmplitude: Int = mRecorder.getMaxAmplitude()
                    visualizerComment?.addAmplitude(maxAmplitude.toFloat())
                    visualizerComment?.invalidate() // refresh the Visualizer
                }
            }
        }

        btnPlayComment.onClick {
            if(mRecorder.isPlaying) {
                btnPlayComment.setImageResource(R.drawable.play)
                mRecorder.pausePlaying()

                // this means that it has a file loaded but it's not currently playing
            } else if (!mRecorder.isReleased) {
                btnPlayComment.setImageResource(R.drawable.pause)
                mRecorder.resumePlaying()

                // it doesn't have a file loaded
            } else {
                currentCommentRecorded?.let {
                    if(it.exists() && it.isFile) {
                        btnPlayComment.setImageResource(R.drawable.pause)
                        mRecorder.startPlaying(it.absolutePath, MediaPlayer.OnCompletionListener { onPlayingCommentFinished() })
                    }
                }
            }
        }
    }

    private fun onPlayingCommentFinished() {
        btnPlayComment.setImageResource(R.drawable.play)
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun audioSetup() {

        // Note: this is not the audio file name, it's a directory.
        val recordingDirectory = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/")
        if(!recordingDirectory.exists()){
            recordingDirectory.mkdir()
        }

        mRecorder = SimpleRecorder(recordingDirectory.absolutePath)
    }

    private fun recordAudio() {

        //Check if all permissions are granted, if not, request again
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            try {
                ActivityCompat.requestPermissions(requireActivity(),
                    PERMISSIONS, PERMISSION_ALL
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            visualizerComment?.visibility = View.VISIBLE
            isRecording = true
            println("RECORD --> START")
            mRecorder.startRecording()
            handlerRecordingComment.post(updater)
        }
    }


    private fun stopAudio() {
        isRecording = false
        handlerRecordingComment.removeCallbacks(updater)
        val filenameRecorded = mRecorder.stopRecording()
        currentCommentRecorded = File(filenameRecorded)
        if(currentCommentRecorded!!.isFile && currentCommentRecorded!!.exists()) {
            showPlayButton()
            btnTrash?.visibility = View.VISIBLE
            visualizerComment.invalidate()
        }
    }

    private fun showPlayButton() {
        btnRecord?.visibility = View.INVISIBLE
        btnPlayComment?.visibility = View.VISIBLE
    }

    private fun showRecordButton() {
        btnRecord?.visibility = View.VISIBLE
        btnPlayComment?.visibility = View.INVISIBLE
    }


    private fun isCommentBarOpen() : Boolean {
        return commentBarUpperSide.visibility == View.VISIBLE
    }

    private fun openCommentBarTextAndFocusIt() {
        view?.showKeyboard()
        etCommentDown.visibility = View.GONE
        etCommentUp.requestFocus()
        commentBarUpperSide.visibility = View.VISIBLE
        etCommentDown.visibility = View.GONE
        visualizerComment.visibility = View.VISIBLE

        val loggedUser = sessionManager.getStoredUser()
        loggedUser?.let {
            Glide.with(context!!)
                .load(it.images.small_url)
                .placeholder(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.circleCropTransform())
                .error(R.mipmap.ic_launcher_round)
                .into(ivUserCommentingPicture)
        }




        if (podcastMode) {
            tvReplyingToCommentBar?.text = tvUserName.text
        } else {
            var firstName = ""
            uiMainCommentWithParent?.comment?.user?.first_name?.let { firstName = it }
            var lastName = ""
            uiMainCommentWithParent?.comment?.user?.last_name?.let { lastName = it }
            val directParentFullname = "$firstName $lastName"

            val parentCount = uiMainCommentWithParent!!.getParentCount() + 1
            if (parentCount == 0) {
                tvReplyingToCommentBar.text =
                    context?.resources?.getString(R.string.replying_to_name)?.let {
                        String.format(
                            it, directParentFullname
                        )
                    }
            } else {
                tvReplyingToCommentBar.text = context?.resources?.getQuantityString(
                    R.plurals.replying_to_sufix, parentCount, directParentFullname, parentCount
                )
            }
        }
    }


    // this function builds the list hierarchy of the first items received in the activity
    // I mean, if we receive a comment of a comment of a comment of a podcast..
    // it will fill the first items in the commentWithParentsItemsList with these first items received
    private fun addTopParents(mainCommentWithParent: CommentWithParent) {
        var currentItem: CommentWithParent? = mainCommentWithParent
        while (currentItem != null) {
            commentWithParentsItemsList.add(currentItem)
            currentItem = currentItem.parent
        }
        commentWithParentsItemsList.reverse()
    }


    private fun initApiCallGetCommentComments() {
        val output = viewModelGetCommentComments.transform(
            GetCommentCommentsViewModel.Input(
                getCommentCommentsDataTrigger
            )
        )

        output.response.observe(this, Observer {
            val newItems = it.data.comments


            if(newItems.size > 0)
                hideEmptyScenario()


            if (isReloading) {
                commentWithParentsItemsList.clear()
                rvComments?.recycledViewPool?.clear()
                isReloading = false
                rvComments?.scrollToPosition(commentWithParentsItemsList.indexOf(uiMainCommentWithParent))
            }


            // if we are in podcast mode, it means that the main publication is the podcast itself,
            // so we have to fill just the lastCommentRequestedReplies because this call is being called
            // from a "show more" button
            if (podcastMode) {

                // we add the new items to its parent
                lastCommentRequestedRepliesParent?.let { parent ->
                    parent.comment.comments.addAll(newItems)

                    // now we have to insert the new items in the global list
                    fillCommentListFromASpecificPosition(
                        lastCommentRequestedRepliesPosition,
                        newItems,
                        parent
                    )
                }


                // if we are not in podcast mode (so we are in comment of comment mode) then the main
                // publication is the uiMainCommentWithParent. This means that we clicked in a specific
                // comment and now we are in this activity and we want to see all the related comments
                // of that specific comment
            } else {

                if (newItems.size > 0) {
                    val firstItem = newItems[0]
                    // if the comments we just received are comments of the mainComment, then we just add it to the list
                    if (firstItem.owner_id == uiMainCommentWithParent?.comment?.id) {
                        fillCommentListWithSpecificParent(newItems, uiMainCommentWithParent)


                        // if they are not comments of the main comment, that means that these new comments
                        // are children of a different comment, so we have to add these children to its
                        // parent comment and then, add the childrens in their position in the main list
                    } else {
                        // we add the new items to its parent
                        lastCommentRequestedRepliesParent?.let { parent ->
                            parent.comment.comments.addAll(newItems)

                            // now we have to insert the new items in the global list
                            fillCommentListFromASpecificPosition(
                                lastCommentRequestedRepliesPosition,
                                newItems,
                                parent
                            )
                        }
                    }
                }
            }



            rvComments?.adapter?.notifyDataSetChanged()
            hideProgressBar()
        })

        output.errorMessage.observe(this, Observer {
            hideProgressBar()
            Toast.makeText(
                context,
                getString(R.string.couldnt_get_feed),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun fillCommentListWithSpecificParent(
        newItems: ArrayList<UIComment>,
        parent: CommentWithParent?
    ) {
        newItems.forEach { comment ->
            val auxComment = CommentWithParent(comment, parent)
            commentWithParentsItemsList.add(auxComment)
            if (comment.comments.size > 0) {
                comment.comments.forEach { subcomment ->
                    commentWithParentsItemsList.add(CommentWithParent(subcomment, auxComment))
                }
            }
        }
    }

    private fun fillCommentListFromASpecificPosition(
        position: Int,
        newItems: ArrayList<UIComment>,
        parent: CommentWithParent
    ) {
        for (i in 0 until newItems.size) {
            commentWithParentsItemsList.add(
                position + i + 1,
                CommentWithParent(newItems[i], parent)
            )
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
        uiPodcast?.let { podcast -> changeItemLikeStatus(podcast, !podcast.liked) }
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

            if(newItems.size > 0)
                hideEmptyScenario()

            if (newItems.size == 0)
                isLastPage = true

            if (isReloading) {
                commentWithParentsItemsList.clear()
                rvComments?.recycledViewPool?.clear()
                isReloading = false
                rvComments?.scrollToPosition(0)
            }

            fillCommentList(newItems)

            rvComments?.adapter?.notifyDataSetChanged()
            hideProgressBar()
        })

        output.errorMessage.observe(this, Observer {
            hideProgressBar()
            Toast.makeText(
                context,
                getString(R.string.couldnt_get_comments),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun fillCommentList(newItems: ArrayList<UIComment>) {
        newItems.forEach { comment ->
            commentWithParentsItemsList.add(CommentWithParent(comment, null))

            if (comment.comments.size > 0) {
                comment.comments.forEach { subComment ->
                    commentWithParentsItemsList.add(
                        CommentWithParent(
                            subComment,
                            CommentWithParent(comment, null)
                        )
                    )
                }
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
                uiPodcast!!,
                object : CommentsAdapter.OnCommentClickListener {
                    override fun onItemClicked(item: CommentWithParent, position: Int) {
                        val podcastDetailsIntent =
                            Intent(context, PodcastDetailsActivity::class.java)
                        podcastDetailsIntent.putExtra("podcast", uiPodcast)
                        podcastDetailsIntent.putExtra("model", item)
                        startActivityForResult(podcastDetailsIntent, 0)
                    }

                    override fun onPlayClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on play", Toast.LENGTH_SHORT).show()
                    }

                    override fun onListenClicked(item: UIComment, position: Int) {
                        Toast.makeText(context, "You clicked on listen", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCommentClicked(item: CommentWithParent, position: Int) {
                        val podcastDetailsIntent =
                            Intent(context, PodcastDetailsActivity::class.java)
                        podcastDetailsIntent.putExtra("podcast", uiPodcast)
                        podcastDetailsIntent.putExtra("model", item)
                        podcastDetailsIntent.putExtra("commenting", true)
                        startActivityForResult(podcastDetailsIntent, 0)
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
                        val podcastByTagIntent = Intent(context, PodcastsByTagActivity::class.java)
                        podcastByTagIntent.putExtra(
                            PodcastsByTagActivity.BUNDLE_KEY_HASHTAG,
                            hashtag
                        )
                        startActivity(podcastByTagIntent)
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

                    override fun onMoreRepliesClicked(parent: CommentWithParent, position: Int) {
                        showProgressBar()
                        lastCommentRequestedRepliesPosition = position
                        lastCommentRequestedRepliesParent = parent
                        viewModelGetCommentComments.idComment = parent.comment.id
                        viewModelGetCommentComments.offset = parent.comment.comments.size
                        getCommentCommentsDataTrigger.onNext(Unit)
                    }

                    override fun onShowLessClicked(
                        parent: CommentWithParent,
                        lastChildPosition: Int
                    ) {
                        val originalParentPosition =
                            lastChildPosition - parent.comment.comments.size
                        removeExcessChildrenFromLists(parent, lastChildPosition)
                        rvComments?.adapter?.notifyDataSetChanged()
                        rvComments?.scrollToPosition(originalParentPosition)
                    }
                },
                podcastMode,
                uiMainCommentWithParent

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
                        if (podcastMode) {
                            viewModelGetPodcastComments.offset =
                                commentWithParentsItemsList.size - 1
                            getPodcastCommentsDataTrigger.onNext(Unit)
                        } else {
                            viewModelGetCommentComments.offset =
                                commentWithParentsItemsList.size - 1
                            getCommentCommentsDataTrigger.onNext(Unit)
                        }
                    }
                }
            }
        })
        rvComments?.isNestedScrollingEnabled = true
        rvComments?.setHasFixedSize(true)
    }

    private fun removeExcessChildrenFromLists(
        parent: CommentWithParent,
        lastChildPosition: Int
    ) {
        val originalParentPosition = lastChildPosition - parent.comment.comments.size
        val childsToRemove = parent.comment.comments.size - Constants.MAX_API_COMMENTS_PER_COMMENT
        for (i in 0 until childsToRemove) {
            parent.comment.comments.removeAt(Constants.MAX_API_COMMENTS_PER_COMMENT)
            commentWithParentsItemsList.removeAt(originalParentPosition + Constants.MAX_API_COMMENTS_PER_COMMENT + 1)
        }
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
        uiPodcast?.id?.let { viewModelCreatePodcastLike.idPodcast = it }


        activity?.let { fragmentActivity ->
            viewModelDeletePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastLikeViewModel::class.java)
        }
        uiPodcast?.id?.let { viewModelDeletePodcastLike.idPodcast = it }


        activity?.let { fragmentActivity ->
            viewModelCreatePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastRecastViewModel::class.java)
        }
        uiPodcast?.id?.let { viewModelCreatePodcastRecast.idPodcast = it }


        activity?.let { fragmentActivity ->
            viewModelDeletePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastRecastViewModel::class.java)
        }
        uiPodcast?.id?.let { viewModelDeletePodcastRecast.idPodcast = it }

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

        activity?.let { fragmentActivity ->
            viewModelCreateCommentComment = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateCommentCommentViewModel::class.java)
        }

        activity?.let { fragmentActivity ->
            viewModelCreatePodcastComment = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastCommentViewModel::class.java)
        }
    }


    private fun fillForm() {
        tvPodcastText?.text = uiPodcast?.caption
        tvPodcastTitle?.text = uiPodcast?.title
        var firstName = ""
        uiPodcast?.user?.first_name?.let { firstName = it }
        var lastName = ""
        uiPodcast?.user?.last_name?.let { lastName = it }
        val fullname = "$firstName $lastName"
        tvUserName?.text = fullname
        tvUserName.onClick { onUserClicked() }


        // datetime and location
        val lat = uiPodcast?.latitude
        val lng = uiPodcast?.longitude
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
        uiPodcast?.created_at?.let {
            datetimeString = CommonsKt.getDateTimeFormattedFromTimestamp(it.toLong())
        }
        val dateAndLocationString = "$datetimeString$locationString"
        tvTimeAndLocation?.text = dateAndLocationString

        // title & caption
        uiPodcast?.title?.let { tvPodcastTitle.text = it }
        uiPodcast?.caption?.let { tvPodcastText.text = hightlightHashtags(it) }
        tvPodcastText?.movementMethod = LinkMovementMethod.getInstance()

        // duration
        uiPodcast?.audio?.duration?.let {
            tvPodcastTime?.text = CommonsKt.calculateDurationMinutesAndSeconds(it.toLong())
        }

        // recasts
        tvRecasts?.onClick { onPodcastRecastClicked() }
        btnRecasts?.onClick { onPodcastRecastClicked() }
        fillFormRecastPodcastData()

        // likes
        tvLikes?.onClick { onPodcastLikeClicked() }
        btnLikes?.onClick { onPodcastLikeClicked() }

        // listens
        uiPodcast?.number_of_listens?.let { tvListens.text = it.toString() }
        tvListens?.onClick { onListensClicked() }
        btnListens?.onClick { onListensClicked() }

        // comments
        uiPodcast?.number_of_comments?.let { tvComments.text = it.toString() }
        tvComments?.onClick { onPodcastCommentClicked() }
        btnComments?.onClick { onPodcastCommentClicked() }

        context?.let {
            // user picture
            Glide.with(it)
                .load(uiPodcast?.user?.images?.small_url)
                .apply(RequestOptions.circleCropTransform())
                .into(ivUserPicture)
            ivUserPicture?.onClick { onUserClicked() }

            // main picture
            Glide.with(it)
                .load(uiPodcast?.images?.medium_url)
                .into(ivMainFeedPicture)
            ivMainFeedPicture.onClick { onItemClicked() }
        }


        // verified
        uiPodcast?.user?.verified?.let {
            if (it)
                ivVerifiedUser.visibility = View.VISIBLE
            else
                ivVerifiedUser.visibility = View.GONE
        } ?: run {
            ivVerifiedUser.visibility = View.GONE
        }


        btnMore?.onClick { onMoreClicked() }
        btnSend?.onClick { onSendClicked() }
        btnPlay?.onClick { onPlayClicked() }

        fillFormLikePodcastData()
    }

    private fun fillFormRecastPodcastData() {
        uiPodcast?.number_of_recasts?.let { tvRecasts.text = it.toString() }
        uiPodcast?.recasted?.let {
            if (it)
                btnRecasts?.setImageResource(R.drawable.recast_filled)
            else
                btnRecasts?.setImageResource(R.drawable.recast)
        } ?: run {
            btnRecasts?.setImageResource(R.drawable.recast)
        }
    }

    private fun fillFormLikePodcastData() {
        uiPodcast?.number_of_likes?.let { tvLikes.text = it.toString() }
        uiPodcast?.liked?.let {
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
        uiPodcast?.audio?.audio_url?.let { _ ->

            AudioService.newIntent(requireContext(), uiPodcast!!, 1L)
                .also { intent ->
                    requireContext().startService(intent)
                    val activity = requireActivity() as BaseActivity
                    activity.showMiniPlayer()
                }

        }
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

    private fun onPodcastCommentClicked() {
        openCommentBarTextAndFocusIt()
    }

    private fun onListensClicked() {
        Toast.makeText(context, "Listens clicked", Toast.LENGTH_SHORT).show()
    }

    private fun onPodcastLikeClicked() {
        uiPodcast?.let { podcast ->
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

    private fun onPodcastRecastClicked() {
        uiPodcast?.let { podcast ->
            changeItemRecastStatus(
                podcast,
                !podcast.recasted
            ) // careful, it will change an item to be from recasted to not recasted and viceversa


            // if now it's recasted, let's call the api
            if (podcast.recasted) {
                viewModelCreatePodcastRecast.idPodcast = podcast.id
                createPodcastRecastDataTrigger.onNext(Unit)

                // if now it's not recasted, let's call the api
            } else {
                viewModelDeletePodcastRecast.idPodcast = podcast.id
                deletePodcastRecastDataTrigger.onNext(Unit)
            }
        }
    }

    private fun onUserClicked() {
        Toast.makeText(context, "User clicked", Toast.LENGTH_SHORT).show()
    }

    private fun configureToolbar() {
        btnClose?.onClick { activity?.finish() }
    }

    private fun onHashtagClicked(clickedTag: String) {
        val podcastByTagIntent = Intent(context, PodcastsByTagActivity::class.java)
        podcastByTagIntent.putExtra(
            PodcastsByTagActivity.BUNDLE_KEY_HASHTAG,
            clickedTag
        )
        startActivity(podcastByTagIntent)
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
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
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
                hashtaggedString.setSpan(clickableSpan, startIndex, endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                println("Hemos encontrado el texto $textFound que empieza en $startIndex y acaba en $endIndex")
            }
            return hashtaggedString
        }
        return SpannableString("")
    }


    private fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar?.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        reloadComments()
    }


    private fun reloadComments() {
        showEmptyScenario()
        showProgressBar()
        app_bar_layout?.setExpanded(true)
        isLastPage = false
        isReloading = true
        commentWithParentsItemsList.clear()
        commentsAdapter?.notifyDataSetChanged()
        if(podcastMode) {
            viewModelGetPodcastComments.offset = 0
            getPodcastCommentsDataTrigger.onNext(Unit)
        } else {
            viewModelGetCommentComments.offset = 0
            getCommentCommentsDataTrigger.onNext(Unit)
        }
    }
}