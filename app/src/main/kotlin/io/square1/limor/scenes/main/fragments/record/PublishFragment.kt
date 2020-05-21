package io.square1.limor.scenes.main.fragments.record

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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.scenes.utils.CommonsKt.Companion.toEditable
import io.square1.limor.uimodels.UIDraft
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PublishFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    lateinit var recordingItem : UIDraft
    private lateinit var mediaPlayer: MediaPlayer
    private var seekHandler = Handler()
    private var run: Runnable? = null
    private var rootView: View? = null
    var app: App? = null
    private val updateDraftTrigger = PublishSubject.create<Unit>()

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
    }



    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)
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
            toast ("Click on Publish button") //TODO JJ implement API CALL
            draftViewModel.uiDraft = UIDraft()
            activity?.finish()
        }


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

