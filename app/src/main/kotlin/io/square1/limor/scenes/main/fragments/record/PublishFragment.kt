package io.square1.limor.scenes.main.fragments.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.hendraanggrian.appcompat.widget.Hashtag
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView
import com.yalantis.ucrop.UCrop
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.Constants
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.scenes.main.fragments.record.adapters.HashtagAdapter
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.scenes.utils.Commons
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.scenes.utils.CommonsKt.Companion.dpToPx
import io.square1.limor.scenes.utils.CommonsKt.Companion.toEditable
import io.square1.limor.scenes.utils.location.MyLocation
import io.square1.limor.scenes.utils.waveform.KeyboardUtils
import io.square1.limor.uimodels.*
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


class PublishFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private lateinit var publishViewModel: PublishViewModel
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var tagsViewModel: TagsViewModel

    lateinit var recordingItem: UIDraft
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
    private var etDraftTitle: EditText? = null
    private var etDraftCaption: SocialAutoCompleteTextView? = null
    private var podcastLocation: UILocations = UILocations("", 0.0, 0.0, false)
    private var imageUrlFinal: String? = ""
    private var audioUrlFinal: String? = ""
    private var isPublished: Boolean = false
    private var listTags = ArrayList<UITags>()
    private var listTagsString: HashtagArrayAdapter<Hashtag>? = null
    private var tvSelectedLocation: TextView? = null
    private var tvSelectedCategory: TextView? = null
    private var twCaption: TextWatcher? = null
    private var twTitle: TextWatcher? = null
    private var rvTags: RecyclerView? = null

    //Flags to publish podcast
    private var audioUploaded: Boolean = false
    private var imageUploaded: Boolean = false
    private var podcastHasImage: Boolean = false
    private var isShowingTagsRecycler = false



    companion object {
        val TAG: String = PublishFragment::class.java.simpleName
        fun newInstance() = PublishFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_publish, container, false)

            audioSeekbar = rootView?.findViewById(R.id.sbProgress)
            timePass = rootView?.findViewById(R.id.tvTimePass)
            timeDuration = rootView?.findViewById(R.id.tvDuration)
            btnPlayPause = rootView?.findViewById(R.id.ibPlayPause)
            btnFfwd = rootView?.findViewById(R.id.ibFfwd)
            btnRew = rootView?.findViewById(R.id.ibRew)
            draftImage = rootView?.findViewById(R.id.ivDraft)
            lytImagePlaceholder = rootView?.findViewById(R.id.lytImagePlaceHolder)
            lytImage = rootView?.findViewById(R.id.lytImage)
            btnSaveDraft = rootView?.findViewById(R.id.btnSaveAsDraft)
            btnPublishDraft = rootView?.findViewById(R.id.btnPublish)
            etDraftTitle = rootView?.findViewById(R.id.etTitle)
            etDraftCaption = rootView?.findViewById(R.id.etCaption)
            tvSelectedLocation = rootView?.findViewById(R.id.tvSelectedLocation)
            tvSelectedCategory = rootView?.findViewById(R.id.tvSelectedCategory)
            lytWithoutTagsRecycler = rootView?.findViewById(R.id.lytWithoutTagsRecycler)
            rvTags = rootView?.findViewById(R.id.rvTags)

            mediaPlayer = MediaPlayer()

            bindViewModel()
            configureMediaPlayerWithButtons()
            updateDraft()
            apiCallPublishPodcast()
            //getCityOfDevice()
            deleteDraft()
            apiCallHashTags()
        }
        app = context?.applicationContext as App
        return rootView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        recordingItem = UIDraft()
        recordingItem = arguments!!["recordingItem"] as UIDraft
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        recordingItem = UIDraft()
        recordingItem = arguments!!["recordingItem"] as UIDraft
        
        configureToolbar()
        listeners()
        loadExistingData()
        multiCompleteText()

        listTagsString = HashtagArrayAdapter<Hashtag>(context!!)
        setupRecyclerTags(listTagsString!!)
    }


    override fun onResume() {
        super.onResume()

        //Load selected location
        podcastLocation = locationsViewModel.locationSelectedItem

        if (!podcastLocation.address.isNullOrEmpty()) {
            tvSelectedLocation?.text = podcastLocation.address
            recordingItem.location = podcastLocation
        }
        if (!publishViewModel.categorySelected.isNullOrEmpty()) {
            tvSelectedCategory?.text = publishViewModel.categorySelected
            recordingItem.category = publishViewModel.categorySelected
            recordingItem.categoryId = publishViewModel.categorySelectedId
        }

        //update database
        callToUpdateDraft()
    }


    private fun apiCallPublishPodcast() {
        val output = publishViewModel.transform(
            PublishViewModel.Input(
                publishPodcastTrigger
            )
        )

        output.response.observe(this, Observer {
            pbPublish?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Publish Ok

                callToDeleteDraft()

                isPublished = true

                try {
                    alert {
                        this.titleResource = R.string.cast_published_ok_title
                        this.messageResource = R.string.cast_published_ok_description
                        okButton {
                            val mainIntent = Intent(context, MainActivity::class.java)
                            startActivity(mainIntent)
                            (activity as RecordActivity).finish()
                        }
                    }.show()
                } catch (e: Exception) {
                }

            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbPublish?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {

                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }

                if (it.code == 10) {  //Session expired
                    alert(message.toString()) {
                        okButton {
                            val intent = Intent(context, SignActivity::class.java)
                            //intent.putExtra(getString(R.string.otherActivityKey), true)
                            startActivityForResult(
                                intent,
                                resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH)
                            )
                        }
                    }.show()
                } else {
                    alert(message.toString()) {
                        okButton { }
                    }.show()
                }

            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }


    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)

            publishViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(PublishViewModel::class.java)

            categoriesViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(CategoriesViewModel::class.java)

            locationsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(LocationsViewModel::class.java)

            tagsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(TagsViewModel::class.java)
        }
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_publish)

        //Toolbar Left
        btnClose.onClick {
            addDataToRecordingItem()

            try {
                draftViewModel.uiDraft = recordingItem

                draftViewModel.filesArray.clear()
                if (!draftViewModel.filesArray.contains(File(recordingItem.filePath))) {
                    draftViewModel.filesArray.add(File(recordingItem.filePath))
                }

                draftViewModel.continueRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.visibility = View.GONE
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {
        lytImagePlaceholder?.onClick {
            loadImagePicker()
        }
        draftImage?.onClick {
            loadImagePicker()
        }

        btnSaveDraft?.onClick {
            addDataToRecordingItem()
            activity?.finish()
        }

        btnPublishDraft?.onClick {
            if (checkEmptyFields()) {
                //In the result of those calls I will call the method readyToPublish() to check their flags
                if(podcastHasImage) {
                    publishPodcastImage()
                }
                publishPodcastAudio()
            }
        }

        lytTvCategory?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_categories)
        }

        lytTvLocation?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_locations)
        }

        //Used for show or hide the recyclerview of the hashtags
        twCaption = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                recordingItem.caption = editable.toString()
                callToUpdateDraft()
            }
            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                if(s.isEmpty()) {
                    rvTags?.visibility = View.GONE
                    lytWithoutTagsRecycler?.visibility = View.VISIBLE
                    isShowingTagsRecycler = false
                } else {
                    val cleanString = s.toString().replace(System.lineSeparator().toString(), " ")
                    val lastSpaceIndex = cleanString.lastIndexOf(" ")
                    val lastWord = if(lastSpaceIndex >= 0) {
                        cleanString.substring(lastSpaceIndex).trim()
                    } else {
                        cleanString.trim()
                    }
                    val pattern = "#\\w+".toRegex()

                    if(pattern.matches(lastWord)) {
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
        etCaption.addTextChangedListener(twCaption)



        //Used for show or hide the recyclerview of the hashtags
        twTitle = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                recordingItem.title = editable.toString()
                callToUpdateDraft()
            }
            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {}
        }
        etTitle.addTextChangedListener(twTitle)


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
        //Upload audio file to AWS
        Commons.getInstance().uploadAudio(
            context,
            File(Uri.parse(recordingItem.filePath).path!!),
            Constants.AUDIO_TYPE_PODCAST,
            object : Commons.AudioUploadCallback {
                override fun onSuccess(audioUrl: String?) {
                    println("Audio upload to AWS succesfully")
                    audioUploaded = true
                    audioUrlFinal = audioUrl
                    readyToPublish()
                }

                override fun onError(error: String?) {
                    audioUploaded = false
                    println("Audio upload to AWS error: $error")
                }
            })
    }


    private fun publishPodcastImage() {

        if (Commons.getInstance().isImageReadyForUpload) {
            //Upload audio file to AWS
            Commons.getInstance().uploadImage(
                context,
                object : Commons.ImageUploadCallback {
                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

                    override fun onSuccess(imageUrl: String?) {
                        println("Image upload to AWS succesfully")
                        //var imageUploadedUrl = imageUrl
                        imageUploaded = true
                        imageUrlFinal = imageUrl
                        readyToPublish()
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {}

                    override fun onError(error: String?) {
                        imageUploaded = false
                        println("Image upload to AWS error: $error")
                    }
                }, Commons.IMAGE_TYPE_ATTACHMENT
            )
        } else {
            val imageFile = File(recordingItem.tempPhotoPath)
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
        pbPublish?.visibility = View.VISIBLE

        val uiPublishRequest = UIPublishRequest(
            podcast = UIPodcastRequest(
                audio = UIAudio(
                    audio_url = audioUrlFinal.toString(),
                    original_audio_url = audioUrlFinal.toString(),
                    duration = mediaPlayer.duration,
                    total_samples = 0.0,
//                    total_length = recordingItem.length!!.toDouble()
                    total_length = mediaPlayer.duration.toDouble() // I just do this because I was told to do it. I prefer to use duration variable
                ),
                meta_data = UIMetaData(
                    title = etDraftTitle?.text.toString(),
                    caption = etDraftCaption?.text.toString(),
                    latitude = podcastLocation.latitude.takeIf { podcastLocation.latitude != 0.0 }
                        ?: 0.0,
                    longitude = podcastLocation.longitude.takeIf { podcastLocation.longitude != 0.0 }
                        ?: 0.0,
                    image_url = imageUrlFinal.toString(),
                    category_id = publishViewModel.categorySelectedId
                )
            )
        )
        publishViewModel.uiPublishRequest = uiPublishRequest
        publishPodcastTrigger.onNext(Unit)
    }


    private fun loadExistingData() {

        //recordingItem
        if (!recordingItem.title.toString().trim().isNullOrEmpty()) {
            //val autosaveText = draftViewModel.uiDraft.title?.toEditable()
            if (!recordingItem.title.toString().trim().equals(getString(R.string.autosave))) {
                etDraftTitle?.text = recordingItem.title?.toEditable()
            }
        }
        if (!recordingItem.caption.toString().trim().isNullOrEmpty()) {
            etDraftCaption?.text = recordingItem.caption?.toEditable()
        }
        if (!recordingItem.tempPhotoPath.toString().trim().isNullOrEmpty()) {
            //Glide.with(context!!).load(draftViewModel.uiDraft.tempPhotoPath).into(draftImage!!)

            val imageFile = File(recordingItem.tempPhotoPath)
            if (!imageFile.path.isNullOrEmpty()) {
                podcastHasImage = true
                Commons.getInstance().handleImage(
                    context,
                    Commons.IMAGE_TYPE_PODCAST,
                    imageFile,
                    "podcast_photo"
                )
            }
            Glide.with(context!!).load(imageFile).into(draftImage!!)  // Uri of the picture
            lytImagePlaceholder?.visibility = View.GONE
            lytImage?.visibility = View.VISIBLE
        } else {
            lytImagePlaceholder?.visibility = View.VISIBLE
            lytImage?.visibility = View.GONE
        }
        if (!recordingItem.category.toString().isNullOrEmpty()) {
            tvSelectedCategory?.text = recordingItem.category
        } else {
            tvSelectedCategory?.text = publishViewModel.categorySelected
            recordingItem.category = publishViewModel.categorySelected
            recordingItem.categoryId = publishViewModel.categorySelectedId
        }
        if (!recordingItem.location?.address.toString().isNullOrEmpty()) {
            tvSelectedLocation?.text = recordingItem.location?.address
        } else {
            tvSelectedLocation?.text = publishViewModel.locationSelectedItem.address
            recordingItem.location = publishViewModel.locationSelectedItem
        }
    }


    private fun addDataToRecordingItem() {
        //Compose the local object
        if (!etDraftTitle?.text.toString().isNullOrEmpty()) {
            recordingItem.title = etDraftTitle?.text.toString()
        } else {
            recordingItem.title = getString(R.string.autosave)
        }

        recordingItem.caption = etDraftCaption?.text.toString()

        //Update Realm
        callToUpdateDraft()
    }


    private fun loadImagePicker() {
        ImagePicker.create(this) // Activity or Fragment
            .showCamera(true) // show camera or not (true by default)
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
            mediaPlayer.setDataSource(recordingItem.filePath)
            mediaPlayer.prepare() // might take long for buffering.
        } catch (e: Exception) {
            //e.printStackTrace()
        }


        //Seekbar progress listener
        audioSeekbar?.max = mediaPlayer.duration
        audioSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
                if (mediaPlayer.duration <= progress) {
                    btnPlayPause?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.play
                        )
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
                        context!!,
                        R.drawable.pause
                    )
                )
                run = Runnable {
                    // Updateing SeekBar every 100 miliseconds
                    audioSeekbar?.progress = mediaPlayer.currentPosition
                    seekHandler.postDelayed(run, 100)
                    //For Showing time of audio(inside runnable)
                    val miliSeconds = mediaPlayer.currentPosition
                    timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(
                        mediaPlayer.duration
                    )
                    timePass?.text = CommonsKt.calculateDurationMediaPlayer(
                        miliSeconds
                    )
//                    if (miliSeconds != 0) {
//                        //if audio is playing, showing current time;
//                        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds.toLong())
//                        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds.toLong())
//                        if (minutes == 0L) {
//                            timePass?.text = "00:" + String.format("%02d", seconds)
//                            timeDuration?.text =
//                                CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
//                        } else {
//                            if (seconds >= 60) {
//                                val sec = seconds - minutes * 60
//                                timePass?.text =
//                                    String.format("%02d", minutes) + ":" + String.format(
//                                        "%02d",
//                                        seconds
//                                    )
//                                timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(
//                                    mediaPlayer.duration
//                                )
//                            }
//                        }
//                    } else {
//                        //Displaying total time if audio not playing
//                        val totalTime = mediaPlayer.duration
//                        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime.toLong())
//                        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime.toLong())
//                        if (minutes == 0L) {
//                            timePass?.text = "00:" + String.format("%02d", seconds)
//                            timeDuration?.text =
//                                CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
//                        } else {
//                            if (seconds >= 60) {
//                                val sec = seconds - minutes * 60
//                                timePass?.text =
//                                    String.format("%02d", minutes) + ":" + String.format(
//                                        "%02d",
//                                        seconds
//                                    )
//                                timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(
//                                    mediaPlayer.duration
//                                )
//                            }
//                        }
//                    }
                }
                run!!.run()
            } else {
                mediaPlayer.pause()
                btnPlayPause?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.play
                    )
                )
            }
        }


        //Forward button
        btnFfwd?.onClick {
            try {
                val nextPosition = mediaPlayer.currentPosition + 30000
                if(nextPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(nextPosition)
                else if(mediaPlayer.currentPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(mediaPlayer.duration)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo forward overflow")
            }
        }


        //Rew button
        btnRew?.onClick {
            try {
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 30000)
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

                // we'll prepare the outputfile
                val croppedImagesDir = File(
                    Environment.getExternalStorageDirectory()?.absolutePath,
                    Constants.LOCAL_FOLDER_CROPPED_IMAGES
                )
                if (!croppedImagesDir.exists()) {
                    val isDirectoryCreated = croppedImagesDir.mkdir()
                }
                val fileName = Date().time.toString() + filesSelected[0].path.substringAfterLast("/")
                val outputFile = File(croppedImagesDir, fileName)

                // and then, we'll perform the crop itself
                performCrop(filesSelected[0].path, outputFile)
            } else {
                lytImage?.visibility = View.GONE
                lytImagePlaceholder?.visibility = View.VISIBLE
            }


            // this will run when coming from the cropActivity and everything is ok
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            podcastHasImage = true
            Glide.with(context!!).load(resultUri).into(draftImage!!)
            lytImage?.visibility = View.VISIBLE
            lytImagePlaceholder?.visibility = View.GONE

            //Add the photopath to recording item
            draftViewModel.uiDraft.tempPhotoPath = resultUri?.path

            Commons.getInstance().handleImage(
                context,
                Commons.IMAGE_TYPE_PODCAST,
                File(resultUri?.path),
                "podcast_photo"
            )

            //Update recording item in Realm
            callToUpdateDraft()

            // this will run when coming from the cropActivity but there is an error
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Timber.d(cropError)
            lytImage?.visibility = View.GONE
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


    private fun performCrop(sourcePath: String, destination: File) {
        val sourceUri = Uri.fromFile(File(sourcePath))
        val destinationUri = Uri.fromFile(destination)
        context?.let {
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1.0f, 1.0f)
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

        output.response.observe(this, Observer {
            if (it) {
                //toast(getString(R.string.draft_updated))
                println("Draft updated")
            } else {
                //toast(getString(R.string.draft_not_updated))
                println("Draft NOT updated")
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            //toast(getString(R.string.draft_not_updated_error))
            println("There was an error updating the draft")
        })
    }


    private fun checkEmptyFields(): Boolean {
        var titleNotEmpty = false
        var captionNotEmpty = false

        //Check the Title of the cast
        if (!etDraftTitle?.text.isNullOrEmpty()) {
            titleNotEmpty = true
        } else {
            alert(getString(R.string.title_cannot_be_empty)) {
                okButton {}
            }.show()
            titleNotEmpty = false
        }

        //Check the Caption of the cast
        if (!etDraftCaption?.text.isNullOrEmpty()) {
            captionNotEmpty = true
        } else {
            alert(getString(R.string.caption_cannot_be_empty)) {
                okButton {}
            }.show()
            captionNotEmpty = false
        }

        return titleNotEmpty && captionNotEmpty
    }


    private fun getCityOfDevice() {

        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
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
        myLocation.getLocation(context!!, locationResult)

    }


    private fun callToTagsApiAndShowRecyclerView(tag: String) {
        tagsViewModel.tagToSearch = tag
        getTagsTrigger.onNext(Unit)
    }


    private fun apiCallHashTags() {
        val output = tagsViewModel.transform(
            TagsViewModel.Input(
                getTagsTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code == 0) {
                if (it.data.tags.size > 0) {
                    listTagsString?.clear()
                    doAsync {
                        listTags.clear()
                        listTags.addAll(it.data.tags)

                        uiThread {
                            for (item in listTags) {
                                listTagsString?.add(Hashtag(item.text))
                            }
                            rvTags?.adapter?.notifyDataSetChanged()
                        }
                    }
                }

            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                println("Error getting the hashtags $message")
            }
        })
    }


    private fun multiCompleteText() {
        etDraftCaption?.isMentionEnabled = false
        etDraftCaption?.hashtagColor = ContextCompat.getColor(context!!, R.color.brandPrimary500)
        etDraftCaption?.hashtagAdapter = listTagsString
        etDraftCaption?.setHashtagTextChangedListener { view, text ->
            println("setHashtagTextChangedListener -> $text")
            callToTagsApiAndShowRecyclerView(text.toString())
        }
    }


    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(context!!)

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


    private fun setupRecyclerTags(tagList: HashtagArrayAdapter<Hashtag>) {
        rvTags?.layoutManager = LinearLayoutManager(context)
        rvTags?.adapter = listTagsString?.let {
            HashtagAdapter(it, object : HashtagAdapter.OnItemClickListener {
                override fun onItemClick(item: Hashtag) {
                    val actualString = etCaption.text
                    val finalString: String =
                        actualString.substring(
                            0, actualString.lastIndexOf(
                                getCurrentWord(
                                    etCaption
                                )!!
                            )
                        ) +
                                "#$item" +
                                actualString.substring(
                                    actualString.lastIndexOf(
                                        getCurrentWord(
                                            etCaption
                                        )!!
                                    ) + getCurrentWord(etCaption)!!.length, actualString.length
                                )

                    etCaption.applyWithDisabledTextWatcher(twCaption!!) {
                        text = finalString
                    }
                    etCaption.setSelection(etCaption.text.length); //This places cursor to end of EditText.
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
        var start = 0
        var end = 0
        var currentWord = ""
        while (matcher.find()) {
            currentWord = matcher.group(matcher.groupCount())
        }
        return currentWord // This is current word
    }


    fun TextView.applyWithDisabledTextWatcher(textWatcher: TextWatcher, codeBlock: TextView.() -> Unit) {
        this.removeTextChangedListener(textWatcher)
        codeBlock()
        this.addTextChangedListener(textWatcher)
    }


    private fun callToDeleteDraft() {
        draftViewModel.uiDraft = recordingItem
        deleteDraftsTrigger.onNext(Unit)
    }


    private fun callToUpdateDraft(){
        draftViewModel.uiDraft = recordingItem
        updateDraftTrigger.onNext(Unit)
    }


    private fun deleteDraft() {
        val output = draftViewModel.deleteDraftRealm(
            DraftViewModel.InputDelete(
                deleteDraftsTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it) {
                println(getString(R.string.draft_deleted))
            } else {
                println(getString(R.string.draft_not_deleted))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
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


    fun View.dpToPx(dp: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics
    ).roundToInt()


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