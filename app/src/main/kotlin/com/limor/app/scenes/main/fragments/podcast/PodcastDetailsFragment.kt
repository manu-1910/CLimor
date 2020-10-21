package com.limor.app.scenes.main.fragments.podcast

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.showKeyboard
import com.limor.app.scenes.main.adapters.CommentsAdapter
import com.limor.app.scenes.main.fragments.profile.ReportActivity
import com.limor.app.scenes.main.fragments.profile.TypeReport
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.service.AudioService
import com.limor.app.uimodels.*
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import kotlinx.android.synthetic.main.include_interactions_bar.*
import kotlinx.android.synthetic.main.include_podcast_data.*
import kotlinx.android.synthetic.main.include_user_bar.*
import kotlinx.android.synthetic.main.toolbar_with_logo_and_back_icon.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
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

    private var lastCommentWithParentDeleted: CommentWithParent? = null
    private var lastPositionCommentDeleted: Int = 0
    private var isBlockingPodcastUser: Boolean = false
    private var btnRecordClicked: Boolean = false
    private var isWaitingForApiCall: Boolean = false
    private var currentOffset: Int = 0
    private var currentCommentRequest: UICreateCommentRequest? = null
    private var currentCommentRecordedDurationMillis: Int = -1
    private var currentCommentRecordedFile: File? = null
    private var isRecording = false
    private val handlerRecordingComment = Handler()
    private lateinit var updater: Runnable

    private lateinit var mRecorder: SimpleRecorder
    private var lastCommentRequestedRepliesPosition: Int = 0
    private var lastCommentRequestedRepliesParent: CommentWithParent? = null
    private var lastLikedItemPosition = 0

    // this variable will be true when we are showing the details of a podcast with the podcast comments
    // it will be false when we are showing the details of a podcast but with the comments of a comment and its parents
    private var podcastMode = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelGetPodcastComments: GetPodcastCommentsViewModel
    private val getPodcastCommentsDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelGetCommentComments: GetCommentCommentsViewModel
    private val getCommentCommentsDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel
    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelDeletePodcastLike: DeletePodcastLikeViewModel
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreatePodcastRecast: CreatePodcastRecastViewModel
    private val createPodcastRecastDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelDeletePodcastRecast: DeletePodcastRecastViewModel
    private val deletePodcastRecastDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateCommentLike: CreateCommentLikeViewModel
    private val createCommentLikeDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelDeleteCommentLike: DeleteCommentLikeViewModel
    private val deleteCommentLikeDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreatePodcastComment: CreatePodcastCommentViewModel
    private val createPodcastCommentDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateCommentComment: CreateCommentCommentViewModel
    private val createCommentCommentDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateCommentReport: CreateCommentReportViewModel
    private val createCommentReportDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateUserReport: CreateUserReportViewModel
    private val createUserReportDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreatePodcastReport: CreatePodcastReportViewModel
    private val createPodcastReportDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelDeletePodcast: DeletePodcastViewModel
    private val deletePodcastDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelDeleteComment: DeleteCommentViewModel
    private val deleteCommentDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelGetPodcastById: GetPodcastByIdViewModel
    private val getPodcastByIdDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateBlockedUser: CreateBlockedUserViewModel
    private val createBlockedUserDataTrigger = PublishSubject.create<Unit>()

    private lateinit var viewModelCreateCommentDropOff : CreateCommentDropOffViewModel
    private val createCommentDropOffDataTrigger = PublishSubject.create<Unit>()


    private val commentWithParentsItemsList = ArrayList<CommentWithParent>()

    private var isLastPage = false
    private var rootView: View? = null
    var app: App? = null

    private var firstTimePadding = true

    // this represents the main podcast of the screen, the postcast that we are seeing the details of
    private var uiPodcast: UIPodcast? = null

    // this represents the main comment of the screen, it will be null when we are in podcastMode
    // and it will be the main comment when we are seeing the comments of this comment
    private var uiMainCommentWithParent: CommentWithParent? = null

    private var audioCommentPlayerController: AudioCommentPlayerController? = null

    private var lastProgressTrackedComment : UIComment? = null
    private var lastProgressTrackedTen : Int = 0
    private lateinit var commentPlayerListener : AudioCommentPlayerController.CommentPlayerListener



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

        private const val REQUEST_REPORT_COMMENT: Int = 0
        private const val REQUEST_REPORT_PODCAST: Int = 1
        private const val REQUEST_REPORT_USER: Int = 2
        private const val REQUEST_PODCAST_DETAILS: Int = 3
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
        initApiCallDeletePodcast()
        initApiCallCreateCommentLike()
        initApiCallDeleteCommentLike()
        initApiCallCreateComment()
        initApiCallCreateCommentReport()
        initApiCallCreatePodcastReport()
        initApiCallCreateUserReport()
        initApiCallCreateBlockedUser()
        initApiCallDeleteComment()
        initApiCallGetPodcastById()
        initApiCallCreateCommentDropOff()
        configureToolbar()

        // we get the possible comment clicked from the previous activity.
        uiMainCommentWithParent = activity?.commentWithParent
        configureAdapter()

        showProgressBar()

        initPodcastOrCommentMode()
        audioSetup()

        fillForm()
        initListeners()
        activity?.startCommenting?.let {
            if(it)
                openCommentBarTextAndFocusIt()
        }
    }


    private fun initApiCallGetPodcastById() {
        val output = viewModelGetPodcastById.transform(
            GetPodcastByIdViewModel.Input(
                getPodcastByIdDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            hideProgressBar()
            val code = response.code
            if (code == 0) {
                uiPodcast = response.data.podcast
                fillFormLikePodcastData()
                fillFormRecastPodcastData()
                fillFormNumberOfCommentsData()
            }
        })

        output.errorMessage.observe(this, Observer {
            hideProgressBar()
        })
    }

    private fun initApiCallDeleteComment() {
        val output = viewModelDeleteComment.transform(
            DeleteCommentViewModel.Input(
                deleteCommentDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                toast(getString(R.string.error_deleting_comment))
            } else {
                // if the main comment is deleted, then we have to close the activity because you can't
                // answer that comment anymore
                if(lastCommentWithParentDeleted == uiMainCommentWithParent) {
                    toast(getString(R.string.comment_deleted_ok))
                    uiPodcast?.number_of_comments?.let {numberOfComments ->
                        uiPodcast?.number_of_comments = numberOfComments - 1
                    }
                    activity?.finish()

                    // if it's a different comment the one that is deleted, then you have to make sure
                    // that its parent has no reference to that deleted comment anymore, and you have to
                    // delete the children of that deleted comment too
                } else {

                    lastCommentWithParentDeleted?.parent?.comment?.let { parent ->
                        parent.comments.remove(lastCommentWithParentDeleted?.comment)
                        parent.comment_count = parent.comment_count.dec()
                    }
                    lastCommentWithParentDeleted?.let { deletedComment -> deleteAllChildComments(lastPositionCommentDeleted, deletedComment) }
                    commentWithParentsItemsList.removeAt(lastPositionCommentDeleted)
                    uiPodcast?.number_of_comments?.let {numberOfComments ->
                        uiPodcast?.number_of_comments = numberOfComments - 1
                    }
                    commentsAdapter?.notifyDataSetChanged()
                }
                fillFormNumberOfCommentsData()
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                alert(message.toString()) {
                    okButton { }
                }.show()
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }

    private fun deleteAllChildComments(lastPositionCommentDeleted: Int, commentDeleted : CommentWithParent) {
        var itemsDeletedCount = 0
        for(i in commentWithParentsItemsList.size - 1 downTo lastPositionCommentDeleted + 1) {
            val currentComment = commentWithParentsItemsList[i]
            if(currentComment.parent == commentDeleted) {
                commentWithParentsItemsList.removeAt(i)
                itemsDeletedCount++
            }
        }

        uiPodcast?.number_of_comments?.let {numberOfComments ->
            uiPodcast?.number_of_comments = numberOfComments - itemsDeletedCount
        }
    }

    private fun initApiCallCreateBlockedUser() {
        val output = viewModelCreateBlockedUser.transform(
            CreateBlockedUserViewModel.Input(
                createBlockedUserDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                toast(getString(R.string.error_blocking_user))
                viewModelCreateBlockedUser.user?.blocked = false
            } else {
                toast(getString(R.string.success_blocking_user))

                // you blocked a podcast user and not a comment user
                if(isBlockingPodcastUser) {
                    activity?.finish()


                }

                // you blocked a comment user
//                else {
//
//                }
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                alert(message.toString()) {
                    okButton { }
                }.show()
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }

    private fun initApiCallCreatePodcastReport() {
        val output = viewModelCreatePodcastReport.transform(
            CreatePodcastReportViewModel.Input(
                createPodcastReportDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(
                    context,
                    getString(R.string.podcast_already_reported),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.podcast_reported_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        output.errorMessage.observe(this, Observer {
            Toast.makeText(
                context,
                getString(R.string.error_report),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun initApiCallCreateUserReport() {
        val output = viewModelCreateUserReport.transform(
            CreateUserReportViewModel.Input(
                createUserReportDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(
                    context,
                    getString(R.string.user_reported_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.user_reported_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        output.errorMessage.observe(this, Observer {
            Toast.makeText(
                context,
                getString(R.string.error_report),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun initApiCallCreateCommentReport() {
        val output = viewModelCreateCommentReport.transform(
            CreateCommentReportViewModel.Input(
                createCommentReportDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(
                    context,
                    getString(R.string.comment_already_reported),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.comment_reported_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        output.errorMessage.observe(this, Observer {
            Toast.makeText(
                context,
                getString(R.string.error_report),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun initPodcastOrCommentMode() {
        // if it's not null, that means that we have to show the "comment of a comment" screen.
        // If it's null this means that we have to show the "comment of a podcast" screen
        isWaitingForApiCall = true
        uiMainCommentWithParent?.let {
            podcastMode = false
            viewModelCreateCommentComment.idComment = it.comment.id
            commentsAdapter?.podcastMode = podcastMode
            hideEmptyScenario()
            addTopParents(it)
            commentsAdapter?.mainCommentPosition = commentWithParentsItemsList.indexOf(it)


            // this is the code that makes the mainComment be on top of the scroll
            rvComments.viewTreeObserver.addOnGlobalLayoutListener {
                if (firstTimePadding) {
                    firstTimePadding = false
                    val newPadding = layNestedScroll.height - rvComments.getChildAt(commentWithParentsItemsList.size - 1).height
                    rvComments?.setPadding(0, 0, 0, newPadding)
                    rvComments?.requestLayout()
                    val newY = rvComments.bottom
                    layNestedScroll.post {
                        layNestedScroll.scrollTo(0, newY)
                    }
                }
            }

            viewModelGetCommentComments.idComment = it.comment.id
            getCommentCommentsDataTrigger.onNext(Unit)

            rvComments?.adapter?.notifyDataSetChanged()

            // but if it is null, we have to get the podcast comments
        } ?: run {
            podcastMode = true
            uiPodcast?.id?.let { idPodcast -> viewModelCreatePodcastComment.idPodcast = idPodcast }
            commentsAdapter?.podcastMode = podcastMode
            uiPodcast?.id?.let { viewModelGetPodcastComments.idPodcast = it }
            getPodcastCommentsDataTrigger.onNext(Unit)
        }
    }

    private fun initApiCallDeletePodcast() {
        val output = viewModelDeletePodcast.transform(
            DeletePodcastViewModel.Input(
                deletePodcastDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                toast(getString(R.string.delete_podcast_error))
            } else {
                toast(getString(R.string.delete_podcast_ok))
                activity?.finish()
            }
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.delete_podcast_error))
        })
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
        layEmptyScenario?.visibility = View.VISIBLE
        rvComments?.visibility = View.GONE
//        app_bar_layout?.setExpanded(true)
//
//        val lp = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
//        lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
//        collapsingToolbar.layoutParams = lp
    }

    private fun hideEmptyScenario() {

        layEmptyScenario?.visibility = View.GONE
        rvComments?.visibility = View.VISIBLE
//
//        val lp = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
//        lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
//        collapsingToolbar.layoutParams = lp
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
                hideEmptyScenario()
                fillFormNumberOfCommentsData()
            }

            hideProgressCreateComment()
            deleteCurrentCommentAudioAndResetBar()
            hideProgressBar()
            hideCommentBar()
            layNestedScroll.post {
                layNestedScroll.scrollTo(0, rvComments.bottom)
            }
        })

        outputPodcast.response.observe(this, Observer {
            it.data?.comment?.let { newComment ->
                addNewCommentToList(newComment)
                hideEmptyScenario()
                fillFormNumberOfCommentsData()
            }
            hideProgressCreateComment()
            deleteCurrentCommentAudioAndResetBar()
            hideProgressBar()
            hideCommentBar()
            layNestedScroll.post {
                layNestedScroll.scrollTo(0, rvComments.bottom)
            }
        })

        outputComment.errorMessage.observe(this, Observer {
            hideProgressBar()
            hideProgressCreateComment()
            Toast.makeText(
                context,
                getString(R.string.couldnt_send_comment),
                Toast.LENGTH_SHORT
            ).show()
        })

        outputPodcast.errorMessage.observe(this, Observer {
            hideProgressBar()
            hideProgressCreateComment()
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
//        hideEmptyScenario()

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


        btnPost.onClick {
            var commentText = ""
            etCommentUp?.text?.let { text -> commentText = text.toString() }
            if(commentText.isNotEmpty() || currentCommentRecordedFile != null) {
                currentCommentRequest = UICreateCommentRequest(UICommentRequest(commentText, 0, null))
                isWaitingForApiCall = true
                showProgressCreateComment()
                if(currentCommentRecordedFile == null) {
                    publishTextComment()
                } else {
                    publishAudioComment()
                }
            } else {
                etCommentUp?.error = getString(R.string.error_comment_empty)
            }
        }

        btnTrash.onClick {
            deleteCurrentCommentAudioAndResetBar()
        }

        btnRecord.onClick {
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
                if (!btnRecordClicked) {
                    btnRecordClicked = true
                    if(!isCommentBarOpen())
                        openCommentBarTextAndFocusIt()

                    btnRecord.setImageResource(R.drawable.record_red)
                    audioSetup()
                    recordAudio()
                } else {
                    btnRecordClicked = false
                    stopAudio()
                    btnRecord.setImageResource(R.drawable.record)
                }
            }
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

        // this is NOT the button to play an another user comment
        // this is the button to play your just recorded comment
        // be careful and don't confuse them
        btnPlayComment.onClick {

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
                if(mRecorder.isPlayerPlaying) {
                    btnPlayComment.setImageResource(R.drawable.play)
                    mRecorder.pausePlaying()

                    // this means that it has a file loaded but it's not currently playing
                } else if (!mRecorder.isPlayerReleased) {
                    btnPlayComment.setImageResource(R.drawable.pause)
                    mRecorder.resumePlaying()

                    // it doesn't have a file loaded
                } else {
                    currentCommentRecordedFile?.let {
                        if(it.exists() && it.isFile) {
                            btnPlayComment.setImageResource(R.drawable.pause)
                            mRecorder.startPlaying(it.absolutePath, MediaPlayer.OnCompletionListener { onPlayingCommentFinished() })
                        }
                    }
                }
            }
        }



        commentPlayerListener = object : AudioCommentPlayerController.CommentPlayerListener {
            override fun onProgress(comment: UIComment, positionInMs: Int) {
                comment.audio.duration?.let {duration ->
                    val durationInMs = duration
                    val currentPercentage = positionInMs.toFloat() * 100f / durationInMs.toFloat()

                    // this is the current 'ten' ('decena' in Spanish).
                    // example:
                    //   50 out of 100 is 50%, it would return 5
                    //   27 out of 100 is 27%, it would return 2
                    //   40 out of 200 is 20%, it would return 2 again
                    val currentTen = (currentPercentage / 10).toInt()
                    if(lastProgressTrackedComment != comment) {
                        lastProgressTrackedComment = comment
                    } else {
                        if(currentTen > lastProgressTrackedTen) {
                            viewModelCreateCommentDropOff.idComment = comment.id
                            viewModelCreateCommentDropOff.percentage = currentPercentage
                            createCommentDropOffDataTrigger.onNext(Unit)
                        }
                    }
                    lastProgressTrackedTen = currentTen
                }
            }

            override fun onCompletion(comment: UIComment) {
                viewModelCreateCommentDropOff.idComment = comment.id
                viewModelCreateCommentDropOff.percentage = 100f
                createCommentDropOffDataTrigger.onNext(Unit)
            }

        }
    }

    private fun deleteCurrentCommentAudioAndResetBar() {
        currentCommentRecordedFile = null
        mRecorder.clear()
        currentCommentRecordedDurationMillis = -1
        currentCommentRecordedFile = null
        visualizerComment.clear()
        visualizerComment.invalidate()
        btnRecord.setImageResource(R.drawable.record)
        btnPlayComment.visibility = View.GONE
        btnRecord.visibility = View.VISIBLE
        btnTrash.visibility = View.GONE
    }

    private fun publishTextComment() {
        if(podcastMode) {
            viewModelCreatePodcastComment.uiCreateCommentRequest = currentCommentRequest!!
            createPodcastCommentDataTrigger.onNext(Unit)
        } else {
            viewModelCreateCommentComment.uiCreateCommentRequest = currentCommentRequest!!
            createCommentCommentDataTrigger.onNext(Unit)
        }
    }

    private fun onPlayingCommentFinished() {
        btnPlayComment.setImageResource(R.drawable.play)
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    private fun audioSetup() {

        // Note: this is not the audio file name, it's a directory.
        // val recordingDirectory  = File(context!!.getExternalFilesDir(null)?.absolutePath, "limorv2");
        val recordingDirectory = File(context?.getExternalFilesDir(null)?.absolutePath, "limorv2")
        var isDirectoryCreated = false
        if(!recordingDirectory.exists()){
            isDirectoryCreated = recordingDirectory.mkdirs()
        }

        mRecorder = SimpleRecorder(recordingDirectory.absolutePath)
    }

    private fun recordAudio() {
        visualizerComment?.visibility = View.VISIBLE
        isRecording = true
        println("RECORD --> START")
        mRecorder.startRecording()
        handlerRecordingComment.post(updater)
    }


    private fun stopAudio() {
        isRecording = false
        handlerRecordingComment.removeCallbacks(updater)
        val filenameAndAudio = mRecorder.stopRecording()
        val filenameRecorded = filenameAndAudio.first
        currentCommentRecordedDurationMillis = filenameAndAudio.second
        currentCommentRecordedFile = File(filenameRecorded)
        if(currentCommentRecordedDurationMillis > 0 && currentCommentRecordedFile!!.isFile && currentCommentRecordedFile!!.exists()) {
            showPlayButton()
            btnTrash?.visibility = View.VISIBLE
            visualizerComment.invalidate()
        }
    }

    private fun publishAudioComment() {

        //Upload audio file to AWS
        Commons.getInstance().uploadAudio(
            context,
            currentCommentRecordedFile,
            Constants.AUDIO_TYPE_COMMENT,
            object : Commons.AudioUploadCallback {
                override fun onSuccess(audioUrl: String?) {
                    Timber.d("Audio uploaded to AWS successfully")
                    currentCommentRequest?.comment?.audio_url = audioUrl
                    currentCommentRequest?.comment?.duration = currentCommentRecordedDurationMillis
                    if(podcastMode) {
                        viewModelCreatePodcastComment.uiCreateCommentRequest = currentCommentRequest!!
                        createPodcastCommentDataTrigger.onNext(Unit)
                    } else {
                        viewModelCreateCommentComment.uiCreateCommentRequest = currentCommentRequest!!
                        createCommentCommentDataTrigger.onNext(Unit)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

                }

                override fun onError(error: String?) {
                    Timber.d("Audio upload to AWS error: $error")
                }
            })
    }



    private fun showPlayButton() {
        btnRecord?.visibility = View.INVISIBLE
        btnPlayComment?.visibility = View.VISIBLE
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

    private fun showProgressCreateComment() {
        progressBarSendComment.visibility = View.VISIBLE
        // you can't set it gone because it will brake the layout, so we set it invisible and non clickable
        btnPost.visibility = View.INVISIBLE
        btnPost.isClickable = false
    }

    private fun hideProgressCreateComment() {
        progressBarSendComment.visibility = View.GONE
        // you can't set it gone because it will brake the layout, so we set it invisible and non clickable
        btnPost.visibility = View.VISIBLE
        btnPost.isClickable = true
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
                // if we don't receive more items and the last call was a comment call of the main comment,
                // then we got to the last page
                if(newItems.size == 0 && viewModelGetCommentComments.idComment == uiMainCommentWithParent?.comment?.id)
                    isLastPage = true

                if (newItems.size > 0) {
                    val firstItem = newItems[0]
                    // if the comments we just received are comments of the mainComment, then we just add it to the list
                    if (firstItem.owner_id == uiMainCommentWithParent?.comment?.id) {
                        fillCommentListWithSpecificParent(newItems, uiMainCommentWithParent)
                        currentOffset += newItems.size


                        if(isWaitingForApiCall)
                            isWaitingForApiCall = false




                        // if they are not comments of the main comment, that means that these new comments
                        // are children of a different comment, so we have to add these children to its
                        // parent comment and then, add the childrens in their position in the main list
                        // this is a "show more" call
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
            val text = when(it.code) {
                4 -> getString(R.string.error_content_unavailable)
                else -> getString(R.string.couldnt_get_feed)
            }
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            ).show()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
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

    private fun initApiCallCreateCommentDropOff() {
        val output = viewModelCreateCommentDropOff.transform(
            CreateCommentDropOffViewModel.Input(
                createCommentDropOffDataTrigger
            )
        )

        output.response.observe(this, Observer {
            Timber.d("Dropoff of comment ${lastProgressTrackedComment?.id} and ten $lastProgressTrackedTen sent successfully ")
        })

        output.errorMessage.observe(this, Observer {
            Timber.d("Error sending dropoff of comment ${lastProgressTrackedComment?.id} and ten $lastProgressTrackedTen")
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
                currentOffset = 0
                isReloading = false
                rvComments?.scrollToPosition(0)
            }

            if(isWaitingForApiCall)
                isWaitingForApiCall = false

            fillCommentList(newItems)
            currentOffset += newItems.size

            rvComments?.adapter?.notifyDataSetChanged()
            hideProgressBar()
        })

        output.errorMessage.observe(this, Observer {
            hideProgressBar()
            val text = when(it.code) {
                4 -> getString(R.string.error_content_unavailable)
                else -> getString(R.string.couldnt_get_feed)
            }
            Toast.makeText(
                context,
                text,
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
                        openNewCommentActivity(item)
                    }

                    override fun onPlayClicked(
                        item: CommentWithParent,
                        position: Int,
                        seekBar: SeekBar,
                        ibtnPlay: ImageButton
                    ) {
                        // if I click in the currently listening comment, then I just launch playclicked
                        if (audioCommentPlayerController != null && audioCommentPlayerController?.comment == item.comment){
                            audioCommentPlayerController?.onPlayClicked()

                            // if you click in a different comment than the one that is currently being listened, we'll firts have to destroy de previous one
                        } else if(audioCommentPlayerController != null && audioCommentPlayerController?.comment != item.comment){

                            // before swapping comment, we have to send a dropoff because the user is not listening to this comment anymore
                            audioCommentPlayerController?.comment?.let {comment ->
                                viewModelCreateCommentDropOff.idComment = comment.id
                                val durationInMs = (comment.audio.duration ?: 0)
                                val currentPlayPositionInMs = audioCommentPlayerController?.getCurrentPlayerPosition() ?: 0
                                val currentPercentage = currentPlayPositionInMs.toFloat() * 100f / durationInMs.toFloat()
                                viewModelCreateCommentDropOff.percentage = currentPercentage
                            }
                            audioCommentPlayerController?.destroy()
                            audioCommentPlayerController = AudioCommentPlayerController(item.comment, seekBar, ibtnPlay, context!!, commentPlayerListener)

                            // if there isn't any comment being listened, just launch this one
                        } else {
                            if (!hasPermissions(requireContext(), *PERMISSIONS)) {
                                try {
                                    ActivityCompat.requestPermissions(requireActivity(),
                                        PERMISSIONS, PERMISSION_ALL
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }else {
                                audioCommentPlayerController =
                                    AudioCommentPlayerController(item.comment, seekBar, ibtnPlay, context!!, commentPlayerListener)
                            }
                        }
                    }

                    override fun onListenClicked(item: UIComment, position: Int) {

                    }

                    override fun onCommentClicked(item: CommentWithParent, position: Int) {
//                        val podcastDetailsIntent =
//                            Intent(context, PodcastDetailsActivity::class.java)
//                        podcastDetailsIntent.putExtra("podcast", uiPodcast)
//                        podcastDetailsIntent.putExtra("model", item)
//                        podcastDetailsIntent.putExtra("commenting", true)
//                        startActivityForResult(podcastDetailsIntent, 0)
                        openNewCommentActivity(item, true)
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
                        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                        userProfileIntent.putExtra("user", item.user)
                        startActivity(userProfileIntent)
                    }

                    override fun onMoreClicked(item: CommentWithParent, position: Int, v: View) {
                        showCommentMorePopupMenu(item, position, v)
                    }

                    override fun onReplyClicked(item: CommentWithParent, position: Int) {
                        openNewCommentActivity(item, true)
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

                    override fun onSeekProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean,
                        currentItem: CommentWithParent,
                        position: Int
                    ) {
                        if(fromUser && audioCommentPlayerController?.comment == currentItem.comment) {
                            audioCommentPlayerController?.onSeekProgressChanged(progress)
                        }
                    }
                },
                podcastMode,
                uiMainCommentWithParent

            )
        }

        rvComments?.adapter = commentsAdapter
        rvComments?.isNestedScrollingEnabled = true
        
        layNestedScroll.setOnScrollChangeListener { v: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            v?.let{
//                if(v.getChildAt(v.childCount - 5) != null) {
//                    if ((scrollY >= (v.getChildAt(v.childCount - 5).measuredHeight - v.measuredHeight)) && scrollY > oldScrollY) {
                if(!isLastPage && !isWaitingForApiCall && rvComments != null && rvComments.visibility == View.VISIBLE) {
                    val goingDown = scrollY > oldScrollY
                    if (goingDown && (scrollY >= (rvComments.measuredHeight - v.measuredHeight))) {

//                        toast("We have to scroll more")
                        isWaitingForApiCall = true
                        if (podcastMode) {
                            viewModelGetPodcastComments.offset = currentOffset
                            getPodcastCommentsDataTrigger.onNext(Unit)
                        } else {
                            viewModelGetCommentComments.offset = currentOffset
                            getCommentCommentsDataTrigger.onNext(Unit)
                        }
                    }
                }
            }
        }
        rvComments?.setHasFixedSize(true)
    }

    private fun openNewCommentActivity(item: CommentWithParent, commenting: Boolean = false) {
        val podcastDetailsIntent =
            Intent(context, PodcastDetailsActivity::class.java)
        podcastDetailsIntent.putExtra("podcast", uiPodcast)
        podcastDetailsIntent.putExtra("model", item)
        podcastDetailsIntent.putExtra("commenting", commenting)
        startActivityForResult(podcastDetailsIntent, REQUEST_PODCAST_DETAILS)
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
            viewModelGetPodcastComments.limit = FEED_LIMIT_REQUEST
            viewModelGetPodcastComments.offset = 0


            viewModelGetCommentComments = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetCommentCommentsViewModel::class.java)
            viewModelGetCommentComments.limit = FEED_LIMIT_REQUEST
            viewModelGetCommentComments.offset = 0


            viewModelCreatePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastLikeViewModel::class.java)
            uiPodcast?.id?.let { viewModelCreatePodcastLike.idPodcast = it }


            viewModelDeletePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastLikeViewModel::class.java)
            uiPodcast?.id?.let { viewModelDeletePodcastLike.idPodcast = it }


            viewModelCreatePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastRecastViewModel::class.java)
            uiPodcast?.id?.let { viewModelCreatePodcastRecast.idPodcast = it }


            viewModelDeletePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastRecastViewModel::class.java)
            uiPodcast?.id?.let { viewModelDeletePodcastRecast.idPodcast = it }


            viewModelCreateCommentLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateCommentLikeViewModel::class.java)


            viewModelDeleteCommentLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeleteCommentLikeViewModel::class.java)


            viewModelCreateCommentComment = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateCommentCommentViewModel::class.java)


            viewModelCreatePodcastComment = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastCommentViewModel::class.java)


            viewModelCreateCommentReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateCommentReportViewModel::class.java)


            viewModelCreatePodcastReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastReportViewModel::class.java)

            viewModelCreateUserReport = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                .get(CreateUserReportViewModel::class.java)

            viewModelDeletePodcast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastViewModel::class.java)

            viewModelCreateBlockedUser = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateBlockedUserViewModel::class.java)

            viewModelDeleteComment = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeleteCommentViewModel::class.java)

            viewModelGetPodcastById = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetPodcastByIdViewModel::class.java)
            uiPodcast?.id?.let { viewModelGetPodcastById.idPodcast = it }

            viewModelCreateCommentDropOff = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreateCommentDropOffViewModel::class.java)
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
//        uiPodcast?.audio?.duration?.let {
        uiPodcast?.audio?.total_length?.let {
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
        fillFormNumberOfCommentsData()
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


        btnMore?.onClick { showPodcastMorePopupMenu() }
        btnSend?.onClick { onSendClicked() }
        btnPlay?.onClick { onPlayPodcastClicked() }

        fillFormLikePodcastData()
    }

    private fun fillFormNumberOfCommentsData() {
        uiPodcast?.number_of_comments?.let {
            tvComments.text = it.toString()
        }
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
    private fun onPlayPodcastClicked() {
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

    private fun showCommentMorePopupMenu(
        comment: CommentWithParent,
        position: Int,
        v: View
    ) {
        val popup = PopupMenu(context, v, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_comment, popup.menu)

        val loggedUser = sessionManager.getStoredUser()
        if(comment.comment.user?.id != loggedUser?.id) {
            // you cannot delete another person's comment
            popup.menu.findItem(R.id.menu_delete_comment).isVisible = false
        } else {
            // you cannot block or report yourself
            popup.menu.findItem(R.id.menu_report_comment).isVisible = false
            popup.menu.findItem(R.id.menu_report_user).isVisible = false
            popup.menu.findItem(R.id.menu_block_user).isVisible = false
        }


        //set menu item click listener here
        popup.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId) {
                R.id.menu_delete_comment -> onDeleteCommentClicked(comment, position)
                R.id.menu_report_comment -> onReportCommentClicked(comment.comment)
                R.id.menu_report_user -> onReportUserClicked(comment.comment.user)
                R.id.menu_block_user -> onCommentUserBlockClicked(comment.comment.user)
            }
            true
        }
        popup.show()
    }

    private fun onDeleteCommentClicked(comment: CommentWithParent, position: Int) {
        alert(getString(R.string.confirmation_delete_comment)) {
            okButton {
                viewModelDeleteComment.comment = comment.comment
                lastPositionCommentDeleted = position
                lastCommentWithParentDeleted = comment
                deleteCommentDataTrigger.onNext(Unit)
            }
            cancelButton {  }
        }.show()
    }

    private fun onCommentUserBlockClicked(user: UIUser?) {
        alert(getString(R.string.confirmation_block_user)) {
            okButton {
                viewModelCreateBlockedUser.user = user
                isBlockingPodcastUser = false
                createBlockedUserDataTrigger.onNext(Unit)
            }
            cancelButton {

            }
        }.show()
    }

    private fun onReportCommentClicked(comment: UIComment) {
        viewModelCreateCommentReport.idCommentToReport = comment.id
        val reportUserIntent = Intent(context, ReportActivity::class.java)
        reportUserIntent.putExtra("type", TypeReport.COMMENT)
        startActivityForResult(reportUserIntent, REQUEST_REPORT_COMMENT)
    }

    private fun showPodcastMorePopupMenu() {
        val popup = PopupMenu(context, btnMore, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_podcast, popup.menu)

        val loggedUser = sessionManager.getStoredUser()
        if(uiPodcast?.user?.id != loggedUser?.id) {
            val menuToHide = popup.menu.findItem(R.id.menu_delete_cast)
            menuToHide.isVisible = false
        } else {
            val menuToHideReportUser = popup.menu.findItem(R.id.menu_report_user)
            menuToHideReportUser.isVisible = false
            val menuToHideReportCast = popup.menu.findItem(R.id.menu_report_cast)
            menuToHideReportCast.isVisible = false
            val menuToHideBlockUser = popup.menu.findItem(R.id.menu_block_user)
            menuToHideBlockUser.isVisible = false
        }

        //set menu item click listener here
        popup.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId) {
                R.id.menu_share -> onSharePodcastClicked()
                R.id.menu_report_cast -> onReportPodcastClicked()
                R.id.menu_report_user -> onReportUserClicked(uiPodcast?.user)
                R.id.menu_delete_cast -> onDeletePodcastClicked()
                R.id.menu_block_user -> onPodcastUserBlockClicked(uiPodcast?.user)
            }
            true
        }
        popup.show()
    }

    private fun onPodcastUserBlockClicked(user: UIUser?) {
        alert(getString(R.string.confirmation_block_user)) {
            okButton {
                viewModelCreateBlockedUser.user = user
                isBlockingPodcastUser = true
                createBlockedUserDataTrigger.onNext(Unit)
            }
            cancelButton {  }
        }.show()
    }

    private fun onDeletePodcastClicked() {
        alert(getString(R.string.confirmation_delete_podcast)) {
            okButton {
                viewModelDeletePodcast.podcast = uiPodcast
                deletePodcastDataTrigger.onNext(Unit)
            }
            cancelButton {  }
        }.show()
    }

    private fun onReportUserClicked(user: UIUser?) {
        user?.let {
            viewModelCreateUserReport.idUser = it.id
            val reportIntent = Intent(context, ReportActivity::class.java)
            reportIntent.putExtra("type", TypeReport.USER)
            startActivityForResult(reportIntent, REQUEST_REPORT_USER)
        }
    }

    private fun onReportPodcastClicked() {
        uiPodcast?.id?.let {
            viewModelCreatePodcastReport.idPodcastToReport = it
            val reportIntent = Intent(context, ReportActivity::class.java)
            reportIntent.putExtra("type", TypeReport.CAST)
            startActivityForResult(reportIntent, REQUEST_REPORT_PODCAST)
        }
    }

    private fun onSharePodcastClicked() {
        uiPodcast?.sharing_url?.let {url ->
            val text = getString(R.string.check_out_this_cast)
            val finalText = "$text $url"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, finalText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        } ?: run {
            toast(getString(R.string.error_retrieving_sharing_url))
        }
    }

    private fun onItemClicked() {

    }

    private fun onPodcastCommentClicked() {
        openCommentBarTextAndFocusIt()
    }

    private fun onListensClicked() {

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
        uiPodcast?.user?.let {
            val userProfileIntent = Intent(context, UserProfileActivity::class.java)
            userProfileIntent.putExtra("user", it)
            startActivity(userProfileIntent)
        }
    }

    private fun configureToolbar() {
        btnClose?.onClick {
            val resultIntent = Intent()
            resultIntent.putExtra("podcast", uiPodcast)
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            activity?.finish()
        }
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
                //println("Hemos encontrado el texto $textFound que empieza en $startIndex y acaba en $endIndex")
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
        if(resultCode == Activity.RESULT_OK) {
            val reason = data?.getStringExtra("reason")
            when (requestCode) {
                REQUEST_REPORT_COMMENT -> {
                    data?.let {
                        viewModelCreateCommentReport.reason = reason
                        createCommentReportDataTrigger.onNext(Unit)
                    }
                }
                REQUEST_REPORT_PODCAST -> {
                    data?.let {
                        viewModelCreatePodcastReport.reason = reason
                        createPodcastReportDataTrigger.onNext(Unit)
                    }
                }
                REQUEST_REPORT_USER -> {
                    data?.let {
                        reason?.let { viewModelCreateUserReport.reason = it }
                        createUserReportDataTrigger.onNext(Unit)
                    }
                }
                REQUEST_PODCAST_DETAILS -> {
                    data?.let {
                        showProgressBar()
                        getPodcastByIdDataTrigger.onNext(Unit)
                    }
                }
            }
        }
//        reloadComments()
    }





//    private fun reloadComments() {
//        showEmptyScenario()
//        showProgressBar()
//        isLastPage = false
//        isReloading = true
//        commentWithParentsItemsList.clear()
//        commentsAdapter?.notifyDataSetChanged()
//        if(podcastMode) {
//            viewModelGetPodcastComments.offset = 0
//            getPodcastCommentsDataTrigger.onNext(Unit)
//        } else {
//            viewModelGetCommentComments.offset = 0
//            getCommentCommentsDataTrigger.onNext(Unit)
//        }
//    }
}