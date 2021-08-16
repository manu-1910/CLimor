package com.limor.app.scenes.main.fragments.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.apollographql.apollo.api.Input
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.textfield.TextInputEditText
import com.hendraanggrian.appcompat.widget.Hashtag
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.CreatePodcastMutation
import com.limor.app.R
import com.limor.app.audio.wav.WavHelper
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.extensions.drawSimpleSelectorDialog
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.record.adapters.HashtagAdapter
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.CommonsKt.Companion.dpToPx
import com.limor.app.scenes.utils.CommonsKt.Companion.toEditable
import com.limor.app.scenes.utils.SpecialCharactersInputFilter
import com.limor.app.scenes.utils.location.MyLocation
import com.limor.app.scenes.utils.waveform.KeyboardUtils
import com.limor.app.type.CreatePodcastInput
import com.limor.app.type.PodcastAudio
import com.limor.app.type.PodcastMetadata
import com.limor.app.uimodels.*
import com.yalantis.ucrop.UCrop
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_profile.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import kotlinx.android.synthetic.main.view_cast_published.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList


class PublishFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private lateinit var publishViewModel: PublishViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var tagsViewModel: TagsViewModel

    lateinit var uiDraft: UIDraft
    private lateinit var mediaPlayer: MediaPlayer
    private var seekHandler = Handler()
    private var run: Runnable? = null
    private var rootView: View? = null
    var app: App? = null

    private val updateDraftTrigger = PublishSubject.create<Unit>()
    private val publishPodcastTrigger = PublishSubject.create<Unit>()
    private val getTagsTrigger = PublishSubject.create<Unit>()
    private val deleteDraftsTrigger = PublishSubject.create<Unit>()

    //Player vars
    private var audioSeekbar: SeekBar? = null
    private var timePass: TextView? = null
    private var timeDuration: TextView? = null
    private var btnPlayPause: ImageButton? = null
    private var btnFfwd: ImageButton? = null
    private var btnRew: ImageButton? = null
    private var draftImage: ImageView? = null
    private var lytImagePlaceholder: RelativeLayout? = null
    private var lytImage: RelativeLayout? = null
    private var lytWithoutTagsRecycler: LinearLayout? = null
    private var btnSaveDraft: Button? = null
    private var btnPublishDraft: Button? = null

    //Form vars
    private var etDraftTitle: TextInputEditText? = null
    private var etDraftCaption: TextInputEditText? = null
    private var etDraftTags: SocialAutoCompleteTextView? = null

    private var podcastLocation: UILocations = UILocations("", 0.0, 0.0, false)
    private var imageUrlFinal: String? = ""
    private var audioUrlFinal: String? = ""
    private var isPublished: Boolean = false
    private var listTags = ArrayList<TagUIModel>()
    private var listTagsString: HashtagArrayAdapter<Hashtag>? = null
    private var tvSelectedLocation: TextView? = null
    private var tvSelectedCategory: TextView? = null
    private var twHastags: TextWatcher? = null
    private var twTitle: TextWatcher? = null
    private var twCaption: TextWatcher? = null
    private var rvTags: RecyclerView? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    //Flags to publish podcast
    private var audioUploaded: Boolean = false
    private var imageUploaded: Boolean = false
    private var podcastHasImage: Boolean = false
    private var isShowingTagsRecycler = false
    private var convertedFile: File? = null

    private var isImageChosen: Boolean = false
    private var isCategorySelected: Boolean = false
    private var isLanguageSelected: Boolean = false
    private var isTagsSelected: Boolean = false

    private var selectedPhotoFile: File? = null

    companion object {
        const val CAMERA_REQUEST = 256
        const val CAMERA_PERMISSION_REQUEST = 345
        val TAG: String = PublishFragment::class.java.simpleName
        fun newInstance() = PublishFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_publish, container, false)

            audioSeekbar = rootView?.findViewById(R.id.sbProgress)
            timePass = rootView?.findViewById(R.id.tvTimePass)
            timeDuration = rootView?.findViewById(R.id.tvDuration)
            btnPlayPause = rootView?.findViewById(R.id.ibPlayPause)
            btnFfwd = rootView?.findViewById(R.id.ibFfwd)
            btnRew = rootView?.findViewById(R.id.ibRew)
            draftImage = rootView?.findViewById(R.id.imageCast)
            lytImagePlaceholder = rootView?.findViewById(R.id.layoutImageCastPlaceholder)
            lytImage = rootView?.findViewById(R.id.layoutCastImage)
            btnSaveDraft = rootView?.findViewById(R.id.btnSaveAsDraft)
            btnPublishDraft = rootView?.findViewById(R.id.btnPublish)
            etDraftTitle = rootView?.findViewById(R.id.etTitle)
            etDraftCaption = rootView?.findViewById(R.id.etCaption)
            etDraftTags = rootView?.findViewById(R.id.etHashtags)
            tvSelectedLocation = rootView?.findViewById(R.id.textSelectedLocation)
            tvSelectedCategory = rootView?.findViewById(R.id.textSelectedCategory)
            lytWithoutTagsRecycler = rootView?.findViewById(R.id.lytWithoutTagsRecycler)
            rvTags = rootView?.findViewById(R.id.rvHashtags)

            mediaPlayer = MediaPlayer()

            bindViewModel()
            configureMediaPlayerWithButtons()
            updateDraft()
            apiCallPublishPodcast()
            getCityOfDevice()
            deleteDraft()
            apiCallHashTags()
        }
        app = context?.applicationContext as App
        return rootView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        uiDraft = requireArguments()["recordingItem"] as UIDraft
    }


    override fun onStart() {
        super.onStart()
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    fun onBackPressed() {
        addDataToRecordingItem()

        if (mediaPlayer.isPlaying)
            mediaPlayer.stop()

        draftViewModel.uiDraft = uiDraft
        draftViewModel.filesArray.clear()
        draftViewModel.filesArray.add(File(uiDraft.filePath))

        findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        uiDraft = requireArguments()["recordingItem"] as UIDraft

        configureToolbar()
        listeners()
        loadExistingData()
        multiCompleteText()

        listTagsString = HashtagArrayAdapter(requireContext())
        setupRecyclerTags()
        updatePublishBtnState()
    }


    override fun onResume() {
        super.onResume()

        val act = requireActivity() as RecordActivity
        act.initScreenBehaviour()
        //Load selected location
        podcastLocation = locationsViewModel.locationSelectedItem

        if (!podcastLocation.address.isNullOrEmpty()) {
            tvSelectedLocation?.text = podcastLocation.address
            uiDraft.location = podcastLocation
        }

        if (publishViewModel.categorySelected.isNotEmpty()) {
            tvSelectedCategory?.text = publishViewModel.categorySelected
            uiDraft.category = publishViewModel.categorySelected
            uiDraft.categoryId = publishViewModel.categorySelectedId
            isCategorySelected = true
            updatePublishBtnState()
        }

        if (publishViewModel.languageSelected.isNotEmpty()) {
            textSelectedLanguage?.text = publishViewModel.languageSelected
            uiDraft.languageCode = publishViewModel.languageCode
            isLanguageSelected = true
            updatePublishBtnState()
        }

        //update database
        callToUpdateDraft()
    }

    private fun updatePublishBtnState() {
        val isTitleValid = etDraftTitle?.text?.trim()?.isNotEmpty() ?: false
        val isCaptionValid = etDraftCaption?.text?.trim()?.isNotEmpty() ?: false
        val isAllRequiredFieldsFilled = isCategorySelected
                && isImageChosen
                && isLanguageSelected
                && isCaptionValid
                && isTitleValid
                && isTagsSelected
        Timber.d(
            "category -> $isCategorySelected " +
                    "imageChosen -> $isImageChosen " +
                    "language -> $isLanguageSelected " +
                    "caption valid -> $isCaptionValid " +
                    "tags-> $isTagsSelected" +
                    "title-> $isTitleValid"
        )
        btnPublishDraft?.isEnabled = isAllRequiredFieldsFilled
    }

    private fun apiCallPublishPodcast() {
       /* val output = publishViewModel.transform(
            PublishViewModel.Input(
                publishPodcastTrigger
            )
        )*/

        publishViewModel.publishResponseData.observe(viewLifecycleOwner,Observer{
            progressPb.visibility = View.GONE
            view?.hideKeyboard()
            it?.let{
                //Publish Ok
                Timber.d("Cast Publish Success -> ")
                convertedFile?.delete()
                callToDeleteDraft()
                isPublished = true
                viewCastPublished.visibility = View.VISIBLE
                btnDone.onClick {
                    val mainIntent = Intent(context, MainActivity::class.java)
                    startActivity(mainIntent)
                    activity?.finish()
                }
            }?:run{
                //Publish Not Ok
                val message: StringBuilder = StringBuilder()
                message.append(getString(R.string.publish_cast_error_message))
                showUnableToPublishCastDialog(
                    description = message.toString(),
                    okText = getString(R.string.try_again),
                    okAction = {
                        btnPublishDraft?.callOnClick()
                    },
                    negativeText = getString(R.string.description_back),
                    negativeAction = {
                    })
            }
        })

       /* output.response.observe(viewLifecycleOwner, Observer {
            progressPb.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Publish Ok
                convertedFile?.delete()
                callToDeleteDraft()
                isPublished = true
                viewCastPublished.visibility = View.VISIBLE
                btnDone.onClick {
                    val mainIntent = Intent(context, MainActivity::class.java)
                    startActivity(mainIntent)
                    activity?.finish()
                }
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            progressPb.visibility = View.GONE

            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {

                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(getString(R.string.publish_cast_error_message))
                }


                if (it.code == 10) {  //Session expired
                    showUnableToPublishCastDialog(
                        getString(R.string.session_expired_error_message),
                        getString(R.string.ok), {
                            val intent = Intent(context, SignActivity::class.java)
                            //intent.putExtra(getString(R.string.otherActivityKey), true)
                            startActivityForResult(
                                intent,
                                resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH)
                            )
                        })
                } else {
                    showUnableToPublishCastDialog(
                        description = message.toString(),
                        okText = getString(R.string.try_again),
                        okAction = {
                            btnPublishDraft?.callOnClick()
                        },
                        negativeText = getString(R.string.description_back),
                        negativeAction = {
                        })
                }

            } else {
                showUnableToPublishCastDialog(
                    description = getString(R.string.default_no_internet),
                    okText = getString(R.string.ok),
                    okAction = {
                    })

            }
        })*/
    }


    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProvider(it, viewModelFactory)
                .get(DraftViewModel::class.java)

            publishViewModel = ViewModelProvider(it, viewModelFactory)
                .get(PublishViewModel::class.java)

            locationsViewModel = ViewModelProvider(it, viewModelFactory)
                .get(LocationsViewModel::class.java)

            tagsViewModel = ViewModelProvider(it, viewModelFactory)
                .get(TagsViewModel::class.java)
        }
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_publish)

        //Toolbar Left
        btnClose.onClick {
            onBackPressed()
        }

        //Toolbar Right
        btnToolbarRight.visibility = View.GONE
    }

    private fun startPublishing() {
        //In the result of those calls I will call the method readyToPublish() to check their flags
        if (podcastHasImage) {
            publishPodcastImage()
        } else {
            publishPodcastAudio()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {
        lytImagePlaceholder?.onClick {
            onSelectImageClicked()
        }
        draftImage?.onClick {
            onSelectImageClicked()
        }
        btnSaveDraft?.onClick {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying)
                mediaPlayer.stop()
            val isTitleEmpty = etDraftTitle?.text.toString().isEmpty()
            if (isTitleEmpty) {
                showSaveAsDraftDialog()
            } else {
                addDataToRecordingItem()
                toast(getString(R.string.draft_inserted))
                activity?.finish()
            }
        }

        btnPublishDraft?.onClick {
            startPublishing()
        }

        layoutCastCategory?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_categories)
        }

        layoutCastLocation?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_locations)
        }

        layoutCastLanguages?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_languages)
        }

        //Used for show or hide the recyclerview of the hashtags
        twHastags = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                etDraftTags?.hashtags?.let{
                    isTagsSelected = it.size > 0
                    publishViewModel.tags.clear()
                    publishViewModel.tags.addAll(it.map { "#$it" })
                    updatePublishBtnState()
                }

            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                if (s.isEmpty()) {
                    rvTags?.visibility = View.GONE
                    lytWithoutTagsRecycler?.visibility = View.VISIBLE
                    isShowingTagsRecycler = false
                } else {
                    val cleanString = s.toString().replace(System.lineSeparator(), " ")
                    val lastSpaceIndex = cleanString.lastIndexOf(" ")
                    val lastWord = if (lastSpaceIndex >= 0) {
                        cleanString.substring(lastSpaceIndex).trim()
                    } else {
                        cleanString.trim()
                    }
                    val pattern = "#\\w+".toRegex()

                    if (pattern.matches(lastWord)) {
                        rvTags?.visibility = View.VISIBLE
                        lytWithoutTagsRecycler?.visibility = View.GONE
                        isShowingTagsRecycler = true

                    } else {
                        rvTags?.visibility = View.GONE
                        lytWithoutTagsRecycler?.visibility = View.VISIBLE
                        isShowingTagsRecycler = false
                    }
                }
            }
        }
        etHashtags.addTextChangedListener(twHastags)


        //Used for show or hide the recyclerview of the hashtags
        twTitle = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                uiDraft.title = editable.toString()
                callToUpdateDraft()
                updatePublishBtnState()
            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {}
        }
        etTitle.addTextChangedListener(twTitle)

        twCaption = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                uiDraft.caption = editable.toString()
                callToUpdateDraft()
                updatePublishBtnState()
            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {}
        }

        etCaption.addTextChangedListener(twTitle)

        //Keyboard listener to hide the recycler
        KeyboardUtils.addKeyboardToggleListener(activity) { isVisible ->
            //println("keyboard visible: $isVisible")
            if (isShowingTagsRecycler) {
                if (isVisible) {
                    rvTags?.visibility = View.VISIBLE
                    lytWithoutTagsRecycler?.visibility = View.GONE
                } else {
                    rvTags?.visibility = View.GONE
                    lytWithoutTagsRecycler?.visibility = View.VISIBLE
                }
            }
        }

    }

    private fun showSaveAsDraftDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_with_edittext, null)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)
        val editText = dialogLayout.findViewById<TextInputEditText>(R.id.editText)
        val titleText = dialogLayout.findViewById<TextView>(R.id.textTitle)

        titleText.text = requireContext().getString(R.string.save_draft_dialog_title)
        editText.filters = arrayOf(SpecialCharactersInputFilter())
        editText.doOnTextChanged { text, start, before, count ->
            positiveButton.isEnabled = count > 0
        }

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)

        val dialog: AlertDialog = dialogBuilder.create()

        positiveButton.onClick {
            uiDraft.title = editText.text.toString()
            uiDraft.caption = etDraftCaption?.text.toString()
            uiDraft.languageCode = publishViewModel.languageCode
            uiDraft.language = publishViewModel.languageSelected
            uiDraft.category = publishViewModel.categorySelected
            uiDraft.categoryId = publishViewModel.categorySelectedId
            callToUpdateDraft()
            toast(getString(R.string.draft_inserted))
            requireActivity().finish()
        }

        cancelButton.onClick {
            dialog.dismiss()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                positiveButton.isEnabled = !p0.isNullOrEmpty()
            }
        })

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset);
            show()
        }
    }

    private fun onSelectImageClicked() {
        drawSimpleSelectorDialog(
            "Select Image Option:",
            listOf("Take A New Photo", "Choose From Gallery")
        )
        { dialog: DialogInterface, i: Int ->
            if (i == 0) {
                captureImage()
            } else {
                loadImagePicker()
            }
            dialog.dismiss()
        }
    }

    private fun captureImage() {
        if (!checkPermissionForCamera()) {
            requestPermissionForCamera()
            return
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        selectedPhotoFile = File(
            requireActivity().externalCacheDir,
            System.currentTimeMillis().toString() + ".jpg"
        )
        intent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                selectedPhotoFile!!
            )
        )
        startActivityForResult(intent, CAMERA_REQUEST)

    }

    private fun readyToPublish() {
        if (podcastHasImage) {
            if (imageUploaded && audioUploaded) {
                imageUploaded = false
                audioUploaded = false
                apiCallPublish()
            }
        } else {
            if (audioUploaded) {
                imageUploaded = false
                audioUploaded = false
                apiCallPublish()
            }
        }
    }


    private fun publishPodcastAudio() {
            progressPb.visibility = View.VISIBLE

            Timber.d("Publishing audio podcast")
            if (!app!!.merlinsBeard!!.isConnected) {

                progressPb.visibility = View.GONE
                showUnableToPublishCastDialog(
                    description = getString(R.string.default_no_internet),
                    okText = getString(R.string.ok),
                    okAction = {

                    })

                return
            }
            convertedFile = WavHelper.convertWavToM4a(requireContext(), uiDraft.filePath!!)
            convertedFile?.let {
                //Upload audio file to AWS
                Commons.getInstance().uploadAudio(
                    context,
                    it,
                    Constants.AUDIO_TYPE_PODCAST,
                    object : Commons.AudioUploadCallback {
                        override fun onSuccess(audioUrl: String?) {
                            audioUploaded = true
                            audioUrlFinal = audioUrl
                            progressPb.visibility = View.GONE
                            readyToPublish()
                        }

                        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                        }

                        override fun onError(error: String?) {
                            progressPb.visibility = View.GONE
                            audioUploaded = false
                            alert(getString(R.string.error_uploading_audio)) {
                                okButton { }
                            }.show()
                            Timber.d("Audio upload to AWS error: $error")
                        }
                    })

            }






    }


    private fun publishPodcastImage() {
        progressPb.visibility = View.VISIBLE

        if (Commons.getInstance().isImageReadyForUpload) {

            // val dialog = AlertProgressBar(requireContext())
            // dialog.show()


            //Upload audio file to AWS
            Commons.getInstance().uploadImage(
                context,
                object : Commons.ImageUploadCallback {
                    override fun onProgressChanged(
                        id: Int,
                        bytesCurrent: Long,
                        bytesTotal: Long
                    ) {
                        //  dialog.updateProgress(bytesCurrent.toInt(), bytesTotal.toInt())
                    }

                    override fun onSuccess(imageUrl: String?) {
                        //var imageUploadedUrl = imageUrl
                        imageUploaded = true
                        imageUrlFinal = imageUrl
                        //dialog.dismiss()
                        publishPodcastAudio()
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {

                    }

                    override fun onError(error: String?) {
                        progressPb.visibility = View.GONE
                        //  dialog.dismiss()
                        imageUploaded = false
                        alert(getString(R.string.error_uploading_image)) {
                            okButton { }
                        }.show()
                        Timber.d("Image upload to AWS error: $error")
                    }
                }, Commons.IMAGE_TYPE_ATTACHMENT
            )
        } else {
            val imageFile = File(uiDraft.tempPhotoPath)
            Commons.getInstance().handleImage(
                context,
                Commons.IMAGE_TYPE_PODCAST,
                imageFile,
                "podcast_photo"
            )
            publishPodcastAudio()
        }
    }


    private fun apiCallPublish() {
        progressPb.visibility = View.VISIBLE
        val podcast = CreatePodcastInput(
                audio = PodcastAudio(
                    audio_url = audioUrlFinal.toString(),
                    original_audio_url = audioUrlFinal.toString(),
                    duration = mediaPlayer.duration,
                    total_samples = 0,
                    total_length = mediaPlayer.duration,
                    sample_rate = 44100.0
                ),
                meta_data = PodcastMetadata(
                    title = etDraftTitle?.text.toString(),
                    caption = etDraftCaption?.text.toString(),
                    latitude = Input.fromNullable(latitude),
                    longitude = Input.fromNullable(longitude),
                    image_url = Input.fromNullable(imageUrlFinal),
                    category_id = publishViewModel.categorySelectedId,
                    language_code = publishViewModel.languageCode,
                )
        )
        Timber.d("$podcast")
        lifecycleScope.launch {
           val response =  withContext(Dispatchers.IO){
               publishViewModel.createPodcast(podcast)
           }
            progressPb.visibility = View.GONE
            response?.let{
            Timber.d("Cast Publish Success -> ")
                convertedFile?.delete()
                callToDeleteDraft()
                isPublished = true
                viewCastPublished.visibility = View.VISIBLE
                btnDone.onClick {
                    /*val mainIntent = Intent(context, MainActivity::class.java)
                    startActivity(mainIntent)*/
                    activity?.finish()
                }
            }?:run{
                //Publish Not Ok
                val message: StringBuilder = StringBuilder()
                message.append(getString(R.string.publish_cast_error_message))
                showUnableToPublishCastDialog(
                    description = message.toString(),
                    okText = getString(R.string.try_again),
                    okAction = {
                        btnPublishDraft?.callOnClick()
                    },
                    negativeText = getString(R.string.description_back),
                    negativeAction = {
                    })
            }
        }
      //  publishViewModel.uiPublishRequest = uiPublishRequest
       // publishPodcastTrigger.onNext(Unit)
    }


    private fun loadExistingData() {

        //recordingItem
        if (!uiDraft.title.toString().trim().isNullOrEmpty()) {
            //val autosaveText = draftViewModel.uiDraft.title?.toEditable()
            if (!uiDraft.title.toString().trim().equals(getString(R.string.autosave))) {
                etDraftTitle?.text = uiDraft.title?.toEditable()
            }
        }
        if (!uiDraft.caption.toString().trim().isNullOrEmpty()) {
            etDraftCaption?.text = uiDraft.caption?.toEditable()
        }
        if (!uiDraft.tempPhotoPath.toString().trim().isNullOrEmpty()) {
            //Glide.with(context!!).load(draftViewModel.uiDraft.tempPhotoPath).into(draftImage!!)

            val imageFile = File(uiDraft.tempPhotoPath)
            if (!imageFile.path.isNullOrEmpty()) {
                podcastHasImage = true
                Commons.getInstance().handleImage(
                    context,
                    Commons.IMAGE_TYPE_PODCAST,
                    imageFile,
                    "podcast_photo"
                )
                isImageChosen = true
            }
            Glide.with(requireContext()).load(imageFile).into(draftImage!!)  // Uri of the picture
            lytImagePlaceholder?.visibility = View.INVISIBLE
            lytImage?.visibility = View.VISIBLE
        } else {
            lytImagePlaceholder?.visibility = View.VISIBLE
            lytImage?.visibility = View.INVISIBLE
        }
        if (!uiDraft.category.toString().isNullOrEmpty()) {
            tvSelectedCategory?.text = uiDraft.category
            this.isCategorySelected = true
        } else {
            tvSelectedCategory?.text = publishViewModel.categorySelected
            uiDraft.category = publishViewModel.categorySelected
            uiDraft.categoryId = publishViewModel.categorySelectedId
        }
        if (!uiDraft.location?.address.toString().isNullOrEmpty()) {
            tvSelectedLocation?.text = uiDraft.location?.address
        } else {
            tvSelectedLocation?.text = publishViewModel.locationSelectedItem.address
            uiDraft.location = publishViewModel.locationSelectedItem
        }
        if (publishViewModel.tags.isNotEmpty()) {
            etHashtags.setText(publishViewModel.tags.joinToString(" "))
            this.isTagsSelected = true
        }
        uiDraft.categoryId?.let{
            publishViewModel.categorySelectedId = it
        }
        uiDraft.languageCode?.let{
            if(it.isNotEmpty())
            publishViewModel.languageCode = it
        }
        uiDraft.language?.let{
            if(it.isNotEmpty())
            publishViewModel.languageSelected = it
        }
        Timber.d("Existing Data: -> $uiDraft")
        updatePublishBtnState()
    }


    private fun addDataToRecordingItem() {
        //Compose the local object
        if (!etDraftTitle?.text.toString().isEmpty()) {
            uiDraft.title = etDraftTitle?.text.toString()
        } else {
            uiDraft.title = getString(R.string.autosave)
        }

        uiDraft.caption = etDraftCaption?.text.toString()

        //Update Realm
        callToUpdateDraft()
    }


    private fun loadImagePicker() {
        ImagePicker.create(this)
            .showCamera(false) // show camera or not (true by default)
            .folderMode(true) // folder mode (false by default)
            .toolbarFolderTitle(getString(R.string.imagepicker_folder)) // folder selection title
            .toolbarImageTitle(getString(R.string.imagepicker_tap_to_select)) // image selection title
            .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
            .includeVideo(true) // Show video on image picker
            .limit(resources.getInteger(R.integer.MAX_PHOTOS)) // max images can be selected (99 by default)
            .theme(R.style.ImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
            .start()
    }


    private fun configureMediaPlayerWithButtons() {

        // Initializing MediaPlayer
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer.setDataSource(uiDraft.filePath)
            mediaPlayer.setOnCompletionListener {
                btnPlayPause?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play
                    )
                )
            }
            mediaPlayer.prepare() // might take long for buffering.
        } catch (e: Exception) {
            //e.printStackTrace()
        }


        //Seekbar progress listener
        audioSeekbar?.max = mediaPlayer.duration
        audioSeekbar?.progress = 0
        audioSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
                if (mediaPlayer.duration <= progress) {
                    btnPlayPause?.setImageDrawable(
                        context?.let {
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_play
                            )
                        }
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        timePass?.text = "00:00"
        timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
        btnPlayPause?.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                btnPlayPause?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_pause
                    )
                )
                run = Runnable {
                    // Updating SeekBar every 100 miliseconds

                    //For Showing time of audio(inside runnable)
                    val miliSeconds = mediaPlayer.currentPosition
                    audioSeekbar?.progress = miliSeconds
                    timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(
                        mediaPlayer.duration
                    )
                    timePass?.text = CommonsKt.calculateDurationMediaPlayer(
                        miliSeconds
                    )
                    seekHandler.postDelayed(run!!, 100)
                }
                run!!.run()
            } else {
                mediaPlayer.pause()
                btnPlayPause?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play
                    )
                )
            }
        }


        //Forward button
        btnFfwd?.onClick {
            try {
                val nextPosition = mediaPlayer.currentPosition + 5000
                if (nextPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(nextPosition)
                else if (mediaPlayer.currentPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(mediaPlayer.duration)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo forward overflow")
            }
        }


        //Rew button
        btnRew?.onClick {
            try {
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 5000)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo rewind overflow")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // this will run when coming from the image picker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked files
            val filesSelected = ImagePicker.getImages(data) as ArrayList<Image>
            if (filesSelected.size > 0) {
                prepareToCrop(filesSelected[0].path)
            } else {
                lytImage?.visibility = View.INVISIBLE
                lytImagePlaceholder?.visibility = View.VISIBLE
            }

            // this will run when coming from camera
        } else if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {
            selectedPhotoFile?.let {
                prepareToCrop(it.path)
            }
            // this will run when coming from the cropActivity and everything is ok
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            podcastHasImage = true
            Glide.with(requireContext()).load(resultUri).into(draftImage!!)
            lytImage?.visibility = View.VISIBLE
            lytImagePlaceholder?.visibility = View.INVISIBLE

            //Add the photopath to recording item
            draftViewModel.uiDraft?.tempPhotoPath = resultUri?.path

            Commons.getInstance().handleImage(
                context,
                Commons.IMAGE_TYPE_PODCAST,
                File(resultUri?.path),
                "podcast_photo"
            )

            //Update recording item in Realm
            callToUpdateDraft()
            isImageChosen = true
            updatePublishBtnState()
            // this will run when coming from the cropActivity but there is an error
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Timber.d(cropError)
            lytImage?.visibility = View.INVISIBLE
            lytImagePlaceholder?.visibility = View.VISIBLE
        }
        context?.let {
            //LOGIN
            if (requestCode == it.resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH) && resultCode == RESULT_OK) {
                loadExistingData()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            Timber.d("Permission Camera")
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) captureImage()
        }
    }

    private fun prepareToCrop(sourcePath: String) {
        val croppedImagesDir = File(
            context?.getExternalFilesDir(null)?.absolutePath,
            Constants.LOCAL_FOLDER_CROPPED_IMAGES
        )
        if (!croppedImagesDir.exists()) {
            val isDirectoryCreated = croppedImagesDir.mkdir()
        }
        val fileName =
            Date().time.toString() + sourcePath.substringAfterLast("/")
        val outputFile = File(croppedImagesDir, fileName)

        // and then, we'll perform the crop itself
        performCrop(sourcePath, outputFile)
    }

    private fun performCrop(sourcePath: String, destination: File) {
        val sourceUri = Uri.fromFile(File(sourcePath))
        val destinationUri = Uri.fromFile(destination)
        context?.let {
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1.0f)
                .withMaxResultSize(1000, 1000)
                .start(it, this, UCrop.REQUEST_CROP)
        }
    }


    private fun updateDraft() {
        val output = draftViewModel.insertDraftRealm(
            DraftViewModel.InputInsert(
                updateDraftTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            if (it) {

//                toast(getString(R.string.draft_saved_ok))
                Timber.d("Draft updated")
            } else {
                toast(getString(R.string.draft_not_updated))
                Timber.d("Draft NOT updated")
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //toast(getString(R.string.draft_not_updated_error))
            println("There was an error updating the draft")
        })
    }


    private fun showUnableToPublishCastDialog(
        description: String,
        okText: String,
        okAction: () -> Unit,
        negativeText: String = getString(R.string.cancel),
        negativeAction: (() -> Unit)? = null
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_error_publish_cast, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog: AlertDialog = dialogBuilder.create()

        dialogView.textDescription.text = description
        dialogView.okButton.text = okText
        if (negativeAction != null) {
            dialogView.cancelButton.visibility = View.VISIBLE
            dialogView.cancelButton.text = negativeText
            dialogView.cancelButton.onClick {
                dialog.dismiss()
                negativeAction.invoke()
            }
        }
        dialogView.okButton.setOnClickListener {
            dialog.dismiss()
            okAction.invoke()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }


    private fun getCityOfDevice() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }


        val locationResult: MyLocation.LocationResult = object : MyLocation.LocationResult() {
            override fun gotLocation(location: Location?) {
                //Got the location!
                val geoCoder = Geocoder(context, Locale.getDefault()) //it is Geocoder
                try {
                    val address: List<Address> = geoCoder.getFromLocation(
                        location!!.latitude,
                        location.longitude,
                        1
                    )
                    location.let {
                        latitude = it.latitude
                        longitude = it.longitude
                    }
                    when {
                        address[0].locality != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].locality
                        }
                        address[0].adminArea != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].adminArea
                        }
                        address[0].thoroughfare != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].thoroughfare
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val myLocation = MyLocation()
        myLocation.getLocation(requireContext(), locationResult)

    }


    private fun callToTagsApiAndShowRecyclerView(tag: String) {
        tagsViewModel.tagToSearch = tag
        tagsViewModel.searchHashTags(tag)
    }


    private fun apiCallHashTags() {
        tagsViewModel.searchResult.observe(viewLifecycleOwner,Observer{
            it?.let{
                if(it.isNotEmpty()){
                    listTagsString?.clear()
                    listTags.clear()
                    listTags.addAll(it)
                    for (item in listTags) {
                        listTagsString?.add(Hashtag(item.tag))
                    }
                    rvTags?.adapter?.notifyDataSetChanged()
                }
            }
        })

    }


    private fun multiCompleteText() {
        etDraftTags?.isMentionEnabled = false
        etDraftTags?.hashtagColor =
            ContextCompat.getColor(requireContext(), R.color.textPrimary)
        etDraftTags?.hashtagAdapter = listTagsString
        etDraftTags?.setHashtagTextChangedListener { _, text ->
            println("setHashtagTextChangedListener -> $text")
            callToTagsApiAndShowRecyclerView(text.toString())
        }
    }


    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(requireContext())

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }


    private fun setupRecyclerTags() {
        rvTags?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTags?.adapter = listTagsString?.let {
            HashtagAdapter(it, object : HashtagAdapter.OnItemClickListener {
                override fun onItemClick(item: Hashtag) {
                    val actualString = etHashtags.text
                    val finalString: String =
                        actualString.substring(
                            0, actualString.lastIndexOf(
                                getCurrentWord(
                                    etHashtags
                                )!!
                            )
                        ) +
                                "#$item" +
                                actualString.substring(
                                    actualString.lastIndexOf(
                                        getCurrentWord(
                                            etHashtags
                                        )!!
                                    ) + getCurrentWord(etHashtags)!!.length, actualString.length
                                ) + " "

                    etHashtags.setText(finalString)
                    etHashtags.setSelection(etHashtags.text.length)
                    //This places cursor to end of EditText.
                    rvTags?.adapter?.notifyDataSetChanged()
                }
            })
        }
    }


    fun getCurrentWord(editText: EditText): String? {
        val textSpan: Spannable = editText.text
        val regex = Regex("#\\w+")
        val pattern: Pattern = Pattern.compile(regex.pattern)
        val matcher: Matcher = pattern.matcher(textSpan)
        var currentWord = ""
        while (matcher.find()) {
            currentWord = matcher.group(matcher.groupCount())
        }
        return currentWord // This is current word
    }


    fun TextView.applyWithDisabledTextWatcher(
        textWatcher: TextWatcher,
        codeBlock: TextView.() -> Unit
    ) {
        this.removeTextChangedListener(textWatcher)
        codeBlock()
        this.addTextChangedListener(textWatcher)
    }


    private fun callToDeleteDraft() {
        draftViewModel.uiDraft = uiDraft
        deleteDraftsTrigger.onNext(Unit)
    }


    private fun callToUpdateDraft() {
        draftViewModel.uiDraft = uiDraft
        updateDraftTrigger.onNext(Unit)
    }

    private fun checkPermissionForCamera(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForCamera() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
    }

    private fun deleteDraft() {
        val output = draftViewModel.deleteDraftRealm(
            DraftViewModel.InputDelete(
                deleteDraftsTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            if (it) {
                println(getString(R.string.draft_deleted))
            } else {
                println(getString(R.string.draft_not_deleted))
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })
        output.errorMessage.observe(this, Observer {
            //toast(getString(R.string.centre_not_deleted_error))
            println("There was an error deleting the draft from DB")
        })
    }


    fun Activity.addKeyboardToggleListener(onKeyboardToggleAction: (shown: Boolean) -> Unit): KeyboardToggleListener? {
        val root = findViewById<View>(android.R.id.content)
        val listener = KeyboardToggleListener(root, onKeyboardToggleAction)
        return root?.viewTreeObserver?.run {
            addOnGlobalLayoutListener(listener)
            listener
        }
    }


    open class KeyboardToggleListener(
        private val root: View?,
        private val onKeyboardToggleAction: (shown: Boolean) -> Unit
    ) : ViewTreeObserver.OnGlobalLayoutListener {
        private var shown = false
        override fun onGlobalLayout() {
            root?.run {
                val heightDiff = rootView.height - height
                val keyboardShown = heightDiff > dpToPx(200f, context)
                if (shown != keyboardShown) {
                    onKeyboardToggleAction.invoke(keyboardShown)
                    shown = keyboardShown
                }
            }
        }
    }


}