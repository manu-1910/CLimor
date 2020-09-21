package io.square1.limor.scenes.main.fragments.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
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
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.hendraanggrian.appcompat.widget.Hashtag
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView
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
import io.square1.limor.scenes.utils.CommonsKt.Companion.toEditable
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
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList


class PublishFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private lateinit var publishViewModel: PublishViewModel
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var tagsViewModel: TagsViewModel

    lateinit var recordingItem : UIDraft
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
    private var tvDraftCategory: TextView? = null
    private var tvDraftLocation: TextView? = null
    private var podcastLocation: UILocations = UILocations("", 0.0, 0.0, false)
    private var imageUrlFinal: String? = ""
    private var audioUrlFinal: String? = ""
    private var isPublished: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val GALLERY_ACTIVITY_CODE = 200
    private val RESULT_CROP = 400
    private val RESULT_CROP_API_29 = 553
    private var listTags = ArrayList<UITags>()
    private var listTagsString: HashtagArrayAdapter<Hashtag>? = null

    //Flags to publish podcast
    private var audioUploaded: Boolean = false
    private var imageUploaded: Boolean = false
    private var podcastHasImage: Boolean = false

    private var tw: TextWatcher? = null



    companion object {
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
            draftImage = rootView?.findViewById(R.id.ivDraft)
            lytImagePlaceholder = rootView?.findViewById(R.id.lytImagePlaceHolder)
            lytImage = rootView?.findViewById(R.id.lytImage)
            btnSaveDraft = rootView?.findViewById(R.id.btnSaveAsDraft)
            btnPublishDraft = rootView?.findViewById(R.id.btnPublish)
            etDraftTitle = rootView?.findViewById(R.id.etTitle)
            etDraftCaption = rootView?.findViewById(R.id.etCaption)

            lytWithoutTagsRecycler = rootView?.findViewById(R.id.lytWithoutTagsRecycler)

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

        tvSelectedLocation.text = podcastLocation.address
        tvSelectedCategory.text = publishViewModel.categorySelected

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
                if(!draftViewModel.filesArray.contains(File(recordingItem.filePath))){
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
    private fun listeners(){
        lytImagePlaceholder?.onClick {
            loadImagePicker()
        }
        draftImage?.onClick {
            loadImagePicker()
        }

        btnSaveDraft?.onClick {
            addDataToRecordingItem()
            //findNavController().navigate(R.id.action_record_publish_to_record_drafts)
            activity?.finish()
        }

        btnPublishDraft?.onClick {
            if (checkEmptyFields()){
                //In the result of those calls I will call the method readyToPublish() to check their flags
                publishPodcastImage()
                publishPodcastAudio()
            }
        }

        lytTvCategory?.onClick{
            findNavController().navigate(R.id.action_record_publish_to_record_categories)
        }

        lytTvLocation?.onClick{
            findNavController().navigate(R.id.action_record_publish_to_record_locations)
        }

        //Used for show or hide the recyclerview of the hashtags
        tw = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                try {
                    if (s.substring(s.length - 1) == "#") {
                        rvTags.visibility = View.VISIBLE
                        lytWithoutTagsRecycler?.visibility = View.GONE
                    } else if ((s.substring(s.length - 1) == " ") || (s.substring(s.length - 1) == System.lineSeparator())){
                        rvTags.visibility = View.GONE
                        lytWithoutTagsRecycler?.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        etCaption.addTextChangedListener(tw)

    }


    private fun readyToPublish(){
        if(podcastHasImage){
            if(imageUploaded && audioUploaded){
                imageUploaded = false
                audioUploaded = false
                apiCallPublish()
            }
        }else{
            if(audioUploaded){
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
                    val error = error
                    println("Audio upload to AWS error: $error")
                }
            })
    }


    private fun publishPodcastImage() {
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
                    var error = error
                    println("Image upload to AWS error: $error")
                }
            }, Commons.IMAGE_TYPE_ATTACHMENT
        )
    }


    private fun apiCallPublish(){
        pbPublish?.visibility = View.VISIBLE

        val uiPublishRequest = UIPublishRequest(
            podcast = UIPodcastRequest(
                audio = UIAudio(
                    audio_url = audioUrlFinal.toString(),
                    original_audio_url = audioUrlFinal.toString(),
                    duration = mediaPlayer.duration,
                    total_samples = 0.0,
                    total_length = recordingItem.length!!.toDouble()
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


    private fun loadExistingData(){

        //recordingItem
        if(!recordingItem.title.toString().trim().isNullOrEmpty()){

            //val autosaveText = draftViewModel.uiDraft.title?.toEditable()
            if(!recordingItem.title.toString().trim().equals(getString(R.string.autosave))){
                etDraftTitle?.text = recordingItem.title?.toEditable()
            }
        }
        if(!recordingItem.caption.toString().trim().isNullOrEmpty()){
            etDraftCaption?.text = recordingItem.caption?.toEditable()
        }
        if(!recordingItem.tempPhotoPath.toString().trim().isNullOrEmpty()){
            //Glide.with(context!!).load(draftViewModel.uiDraft.tempPhotoPath).into(draftImage!!)

            val imageFile = File(recordingItem.tempPhotoPath)
            if (!imageFile.path.isNullOrEmpty()){
                podcastHasImage = true
            }
            Glide.with(context!!).load(imageFile).into(draftImage!!)  // Uri of the picture
            lytImagePlaceholder?.visibility = View.GONE
            lytImage?.visibility = View.VISIBLE
        }else{
            lytImagePlaceholder?.visibility = View.VISIBLE
            lytImage?.visibility = View.GONE
        }
    }


    private fun addDataToRecordingItem(){
        //Compose the local object
        if(!etDraftTitle?.text.toString().isNullOrEmpty()){
            recordingItem.title = etDraftTitle?.text.toString()
        }else{
            recordingItem.title = getString(R.string.autosave)
        }

        if(!etDraftCaption?.text.toString().isNullOrEmpty()){
            recordingItem.caption = etDraftCaption?.text.toString()
        }else{
            recordingItem.caption = "autosave caption"
        }

        //recordingItem.hastags
        //recordingItem.location
        
        //Compose the viewmodel object
        draftViewModel.uiDraft.title = etDraftTitle?.text.toString()
        draftViewModel.uiDraft.caption = etDraftCaption?.text.toString()
        //draftViewModel.uiDraft.hashtags = etdrafthashtangs?.text.toString()
        //draftViewModel.uiDraft.location = etdraftlocation?.text.toString()
        draftViewModel.uiDraft.filePath = recordingItem.filePath
        draftViewModel.uiDraft.editedFilePath = recordingItem.filePath

        //Update Realm
        updateDraftTrigger.onNext(Unit)

    }


    private fun loadImagePicker(){
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

//        ImagePicker.with(this)
//            .crop()	    			//Crop image(Optional), Check Customization for more option
//            .compress(1024)			//Final image size will be less than 1 MB(Optional)
//            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
//            .start()
    }


    private fun configureMediaPlayerWithButtons(){

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
                    if (miliSeconds != 0) {
                        //if audio is playing, showing current time;
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds.toLong())
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds.toLong())
                        if (minutes == 0L) {
                            timePass?.text = "00:" + String.format("%02d", seconds)
                            timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                timePass?.text = String.format("%02d", minutes)+":"+String.format(
                                    "%02d",
                                    seconds
                                )
                                timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(
                                    mediaPlayer.duration
                                )
                            }
                        }
                    } else {
                        //Displaying total time if audio not playing
                        val totalTime = mediaPlayer.duration
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime.toLong())
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime.toLong())
                        if (minutes == 0L) {
                            timePass?.text = "00:" + String.format("%02d", seconds)
                            timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                timePass?.text = String.format("%02d", minutes)+":"+String.format(
                                    "%02d",
                                    seconds
                                )
                                timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(
                                    mediaPlayer.duration
                                )
                            }
                        }
                    }
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
                mediaPlayer.seekTo(30000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        //Rew button
        btnRew?.onClick {
            try {
                mediaPlayer.seekTo(-30000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked files
            val filesSelected = ImagePicker.getImages(data) as ArrayList<Image>
            if (filesSelected.size > 0) {
                performCrop(filesSelected[0].path)
            }else{
                lytImage?.visibility = View.GONE
                lytImagePlaceholder?.visibility = View.VISIBLE
            }
        }

        if (requestCode == RESULT_CROP){
            if(resultCode == Activity.RESULT_OK){
                val outputFile: File?

                // if we come from the cropResultActivity and data is not null, it means
                // that we are in api > 23, so we have to handle the image like this
                val contentUri: Uri? = data?.data
                if(contentUri != null) {

                    // our contentUri won't be null, so we'll get an inputstream from that uri
                    val inputStream : InputStream? = context?.contentResolver?.openInputStream(contentUri)

                    // after that, we'll have to create a file from that stream, we'll use this folder
                    val croppedImagesDir =
                        File(Environment.getExternalStorageDirectory()?.absolutePath, Constants.LOCAL_FOLDER_CROPPED_IMAGES)
                    if (!croppedImagesDir.exists()) {
                        val isDirectoryCreated = croppedImagesDir.mkdir()
                    }

                    val fileName = Date().time.toString() + contentUri.path?.substringAfterLast("/")
                    outputFile = File(croppedImagesDir, fileName)
                    outputFile.createNewFile()
                    outputFile.outputStream().use { inputStream?.copyTo(it) }

                    if(!outputFile.exists())
                        Timber.d("Image doesn't exist")

                    if(!outputFile.isFile)
                        Timber.d("Image doesn't exist")


                    // if we come from crop activity and data is null, that means that we are under
                    // api <= 23, so we have to handle it like this
                } else {
                    val bundle = data?.extras
                    val image = bundle?.get("data") as Bitmap

                    val uri = bitmapToFile(image)
                    val filePath = uri.path
                    // we have to create a file from that uri too
                    outputFile = File(filePath)
                }

                if(draftImage != null){
                    Glide.with(context!!).load(outputFile).into(draftImage!!)
                    podcastHasImage = true
                }else{
                    podcastHasImage = false
                }

                //Add the photopath to recording item
                draftViewModel.uiDraft.tempPhotoPath = outputFile.path

                Commons.getInstance().handleImage(
                    context,
                    Commons.IMAGE_TYPE_PODCAST,
                    outputFile,
                    "podcast_photo"
                )

                //Update recording item in Realm
                updateDraftTrigger.onNext(Unit)

                lytImage?.visibility = View.VISIBLE
                lytImagePlaceholder?.visibility = View.GONE
            }else{
                lytImage?.visibility = View.GONE
                lytImagePlaceholder?.visibility = View.VISIBLE
            }
        }

        context?.let {
            //LOGIN
            if (requestCode == it.resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH) && resultCode == Activity.RESULT_OK) {
                loadExistingData()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun performCrop(picUri: String) {
        try {
            //Start Crop Activity
            val cropIntent = Intent("com.android.camera.action.CROP")
            // indicate image type and Uri
            val f = File(picUri)
            val contentUri: Uri

            if (Build.VERSION.SDK_INT > M) {
                contentUri = FileProvider.getUriForFile(
                    context!!,
                    "io.square1.limor.app.provider",
                    f
                ) //package.provider

                getApplicationContext().grantUriPermission(
                    "com.android.camera",
                    contentUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri) //For android API 29
            } else {
                contentUri = Uri.fromFile(f)
            }


            cropIntent.setDataAndType(contentUri, "image/*")
            // set crop properties
            cropIntent.putExtra("crop", "true")
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280)
            cropIntent.putExtra("outputY", 280)

            // retrieve data on return
            cropIntent.putExtra("return-data", true)
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP)
        } // respond to users whose devices do not support the crop action
        catch (anfe: ActivityNotFoundException) {
            // display an error message
            val errorMessage = "Your device doesn't support the crop action!"
            toast(errorMessage)
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
            } else {
                //toast(getString(R.string.draft_not_updated))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_updated_error))
        })
    }


    private fun checkEmptyFields(): Boolean{

        println("---------------DOUBLE Start of checkEmptyFields()")
        var titleNotEmpty = false
        var captionNotEmpty = false

        //Check the Title of the cast
        if(!etDraftTitle?.text.isNullOrEmpty()) {
            titleNotEmpty = true
        }else{
            alert(getString(R.string.title_cannot_be_empty)) {
                okButton {}
            }.show()
            titleNotEmpty = false
        }

        //Check the Caption of the cast
        if(!etDraftCaption?.text.isNullOrEmpty()) {
            captionNotEmpty = true
        }else{
            alert(getString(R.string.caption_cannot_be_empty)) {
                okButton {}
            }.show()
            captionNotEmpty = false
        }

        if(titleNotEmpty && captionNotEmpty){
            return true
        }else {
            return false
        }
    }


    private fun getCityOfDevice(){

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                //println("location es: " +location)
                val geoCoder = Geocoder(context, Locale.getDefault()) //it is Geocoder
                try {
                    val address: List<Address> = geoCoder.getFromLocation(
                        location!!.latitude,
                        location.longitude,
                        1
                    )
                    locationsViewModel.uiLocationsRequest.term = address[0].locality
                    //println("address is " + city)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
    }


    private fun callToTagsApiAndShowRecyclerView(tag: String){
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


    private fun multiCompleteText(){
        //listTagsString = HashtagAdapter(context!!)
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

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
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
                            actualString.substring(0, actualString.lastIndexOf(getCurrentWord(etCaption)!!))+
                                    "#$item" +
                                    actualString.substring(actualString.lastIndexOf(getCurrentWord(etCaption)!!) + getCurrentWord(etCaption)!!.length, actualString.length)

                        etCaption.applyWithDisabledTextWatcher(tw!!) {
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


    private fun callToDeleteDraft(){
        draftViewModel.uiDraft = recordingItem
        deleteDraftsTrigger.onNext(Unit)
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
            toast(getString(R.string.centre_not_deleted_error))
        })
    }

}

