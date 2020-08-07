package io.square1.limor.scenes.main.fragments.record

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.Constants
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.main.viewmodels.LocationsViewModel
import io.square1.limor.scenes.main.viewmodels.PublishViewModel
import io.square1.limor.scenes.main.viewmodels.TagsViewModel
import io.square1.limor.scenes.utils.Commons
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.scenes.utils.CommonsKt.Companion.toEditable
import io.square1.limor.uimodels.*
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PublishFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private lateinit var publishViewModel: PublishViewModel
    private lateinit var hashtagViewModel: TagsViewModel
    private lateinit var locationsViewModel: LocationsViewModel


    lateinit var recordingItem : UIDraft
    private lateinit var mediaPlayer: MediaPlayer
    private var seekHandler = Handler()
    private var run: Runnable? = null
    private var rootView: View? = null
    var app: App? = null
    private val updateDraftTrigger = PublishSubject.create<Unit>()
    private val publishPodcastTrigger = PublishSubject.create<Unit>()

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
    private var btnSaveDraft: Button? = null
    private var btnPublishDraft: Button? = null

    private var etDraftTitle: EditText? = null
    private var etDraftCaption: EditText? = null
    private var etDraftHashtags: EditText? = null
    private var etDraftLocation: EditText? = null

    private var lytHashtags: RelativeLayout? = null
    private var lytLocation: RelativeLayout? = null

    private var podcastLocation: UILocations = UILocations("", 0.0, 0.0, false)




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
            etDraftHashtags = rootView?.findViewById(R.id.etHashtags)
            etDraftLocation = rootView?.findViewById(R.id.etLocation)

            lytHashtags = rootView?.findViewById(R.id.lytHashtags)
            lytLocation = rootView?.findViewById(R.id.lytLocation)

            mediaPlayer = MediaPlayer()
        }
        app = context?.applicationContext as App
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        recordingItem = UIDraft()
        recordingItem = arguments!!["recordingItem"] as UIDraft

        bindViewModel()
        configureToolbar()
        listeners()
        configureMediaPlayerWithButtons()
        loadExistingData()
        updateDraft()
        apiCallPublishPodcast()
    }

    override fun onResume() {
        super.onResume()

        //Load selected hashtags
        var hashtagListString: String = ""
        for (tag in hashtagViewModel.localListTagsSelected){
            hashtagListString = "#"+tag.text+", "
        }
        etHashtags.text = hashtagListString.toEditable()

        //Load selected location
        podcastLocation = locationsViewModel.locationSelectedItem
        etLocation.text = podcastLocation.address.toEditable()
    }


    private fun apiCallPublishPodcast() {
        val output = publishViewModel.transform(
            PublishViewModel.Input(
                publishPodcastTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Publish Ok

                alert {
                    this.titleResource = R.string.cast_published_ok_title
                    this.messageResource = R.string.cast_published_ok_description
                    okButton {
                        val mainIntent = Intent(context, MainActivity::class.java)
                        startActivity(mainIntent)
                        (activity as RecordActivity).finish()
                    }
                }.show()

            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {

                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }


                if(it.code == 10){  //Session expired
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

                }else{
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

            hashtagViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(TagsViewModel::class.java)
            locationsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(LocationsViewModel::class.java)
        }
    }



    private fun configureToolbar() {

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_publish)

        //Toolbar Left
        btnClose.onClick {
            addDataToRecordingItem()
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.visibility = View.GONE

    }


    private fun listeners(){
        lytImagePlaceholder?.onClick {
            loadImagePicker()
        }
        draftImage?.onClick {
            loadImagePicker()
        }

        btnSaveDraft?.onClick {
            addDataToRecordingItem()
            findNavController().navigate(R.id.action_record_publish_to_record_drafts)
        }

        btnPublishDraft?.onClick {
            publishPodcast()
        }

        lytHashtags?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_hashtags)
        }

        lytLocation?.onClick {
            findNavController().navigate(R.id.action_record_publish_to_record_locations)
        }
    }

    private fun publishPodcast() {

        //Upload audio file to AWS
        Commons.getInstance().uploadAudio(
            context,
            File(Uri.parse(recordingItem.filePath).path!!),
            Constants.AUDIO_TYPE_PODCAST,
            object : Commons.AudioUploadCallback {
                override fun onSuccess(audioUrl: String?) {
                    println("Audio upload to AWS succesfully")
                    var url = audioUrl

                    //var imageFile = File(Uri.parse(draftImage))

                    var uiPublishRequest = UIPublishRequest(
                        podcast = UIPodcastRequest(
                            audio = UIAudio(
                                audio_url = url!!,
                                original_audio_url = url,
                                duration = mediaPlayer.duration,
                                total_samples = 0.0,
                                total_length = recordingItem.length!!.toDouble(),
                                sample_rate = 0.0,
                                timestamps = ArrayList() //recordingItem.timeStamps
                            ),
                            meta_data = UIMetaData(
                                title =  etDraftTitle?.text.toString(),
                                caption = etDraftCaption?.text.toString(),
                                latitude = podcastLocation?.latitude.takeIf { podcastLocation?.latitude != 0.0 } ?:  0.0,
                                longitude = podcastLocation?.longitude.takeIf { podcastLocation?.longitude != 0.0 } ?:  0.0,
                                image_url = "https://www.google.com/url?sa=i&url=https%3A%2F%2Funsplash.com%2Fexplore&psig=AOvVaw32sorEzNMl6cJEfuMBBExZ&ust=1596609860902000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCLjZ4q75gOsCFQAAAAAdAAAAABAE"
                            )
                        )
                    )

                    publishViewModel.uiPublishRequest = uiPublishRequest

                    //TODO set the pusblishRequest and set it to the viewmodel

                    publishPodcastTrigger.onNext(Unit)
                }

                override fun onError(error: String?) {
                    var error= error
                    println("Audio upload to AWS error: $error")

                }
            })
    }


    private fun loadExistingData(){
        if(!draftViewModel.uiDraft.title.toString().trim().isNullOrEmpty()){
            etDraftTitle?.text = draftViewModel.uiDraft.title?.toEditable()
        }
        if(!draftViewModel.uiDraft.caption.toString().trim().isNullOrEmpty()){
            etDraftCaption?.text = draftViewModel.uiDraft.caption?.toEditable()
        }
        if(!draftViewModel.uiDraft.tempPhotoPath.toString().trim().isNullOrEmpty()){
            //Glide.with(context!!).load(draftViewModel.uiDraft.tempPhotoPath).into(draftImage!!)

            var imageFile = File(draftViewModel.uiDraft.tempPhotoPath)
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
        recordingItem.title = etDraftTitle?.text.toString()
        recordingItem.caption = etDraftCaption?.text.toString()
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
            .includeVideo(false) // Show video on image picker
            .limit(resources.getInteger(R.integer.MAX_PHOTOS)) // max images can be selected (99 by default)
            .theme(R.style.ImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
            .start()
    }


    private fun configureMediaPlayerWithButtons(){

        // Initializing MediaPlayer
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer.setDataSource(recordingItem.filePath)
            mediaPlayer.prepare() // might take long for buffering.
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //Seekbar progress listener
        audioSeekbar?.max = mediaPlayer.duration
        audioSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
                if(mediaPlayer.duration <= progress){
                    btnPlayPause?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play))
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
                btnPlayPause?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pause))
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
                                timePass?.text = String.format("%02d", minutes)+":"+String.format("%02d", seconds)
                                timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
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
                                timePass?.text = String.format("%02d", minutes)+":"+String.format("%02d", seconds)
                                timeDuration?.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.duration)
                            }
                        }
                    }
                }
                run!!.run()
            } else {
                mediaPlayer.pause()
                btnPlayPause?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play))
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
            //isFromImagePicker = true

            if (filesSelected.size > 0) {
                Glide.with(context!!).load(filesSelected[0].path).into(draftImage!!)
                //Add the photopath to recording item
                draftViewModel.uiDraft.tempPhotoPath = filesSelected[0].path
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
//            //FAVOURITES PROPERTY DETAILS
//            if (requestCode == it.resources.getInteger(R.integer.REQUEST_CODE_FROM_RECENTLY_ADDED_TO_PROPERTY_DETAIL) && resultCode == Activity.RESULT_OK) {
//                favouritePropertyClicked =
//                    data?.getSerializableExtra(getString(R.string.propertyKey)) as UIProperty
//                for (item in recentlyAddedList) {
//                    if (item is UIProperty)
//                        if (item.PropertyId == favouritePropertyClicked.PropertyId) {
//                            item.IsFavourite = favouritePropertyClicked.IsFavourite
//                            rvFilteredRecentlyAdded?.adapter?.notifyDataSetChanged()
//                        }
//                }
//            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun updateDraft() {
        val output = draftViewModel.insertDraftRealm(
            DraftViewModel.InputInsert(
                updateDraftTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it) {
                toast(getString(R.string.draft_updated))
            } else{
                toast(getString(R.string.draft_not_updated))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_updated_error))
        })
    }





}

