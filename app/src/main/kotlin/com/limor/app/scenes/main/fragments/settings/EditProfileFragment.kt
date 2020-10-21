package com.limor.app.scenes.main.fragments.settings


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.yalantis.ucrop.UCrop
import io.reactivex.subjects.PublishSubject
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.CommonsKt.Companion.ageToTimestamp
import com.limor.app.scenes.utils.CommonsKt.Companion.timestampToAge
import com.limor.app.scenes.utils.CommonsKt.Companion.toEditable
import com.limor.app.uimodels.UIErrorResponse
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


class EditProfileFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var updateUserViewModel: UpdateUserViewModel
    private val updateUserTrigger = PublishSubject.create<Unit>()

    private var rootView: View? = null
    var app: App? = null
    var profileImageUploaded = true
    var profileHasImage = false
    var profileImageUrlFinal = ""
    var tempPhotoPath = ""


    companion object {
        val TAG: String = EditProfileFragment::class.java.simpleName
        fun newInstance() = EditProfileFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        }
        app = context?.applicationContext as App
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        configureToolbar()
        listeners()
        loadExistingData()
        apiCallUpdateUser()
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.settings_edit_profile)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btnUpdate)
        btnToolbarRight.visibility = View.VISIBLE
        btnToolbarRight.onClick {

            pbEditProfile?.visibility = View.VISIBLE

            if(profileHasImage){
                publishProfileImage()
            }else{
                callToApiUpdateUser()
            }

        }
    }


    private fun bindViewModel() {
        activity?.let {
            updateUserViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(UpdateUserViewModel::class.java)
        }
    }


    private fun listeners(){
        btnChoosePhoto.onClick {
            loadImagePicker()
        }
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


    private fun callToApiUpdateUser(){
        updateUserViewModel.first_name = etFirstName.text.toString()
        updateUserViewModel.last_name = etLastName.text.toString()
        updateUserViewModel.username = etUsername.text.toString()
        updateUserViewModel.website = etWebsite.text.toString()
        updateUserViewModel.description = etBio.text.toString()
        updateUserViewModel.email = etEmail.text.toString()
        updateUserViewModel.phone_number = etPhone.text.toString()
        updateUserViewModel.date_of_birth =  ageToTimestamp(etAge.text.toString().toInt())
        updateUserViewModel.gender = etGender.text.toString()
        updateUserViewModel.notifications_enabled = sessionManager.getStoredUser()!!.notifications_enabled
        if(profileHasImage){
            updateUserViewModel.image = profileImageUrlFinal
        }

        updateUserTrigger.onNext(Unit)
    }


    private fun apiCallUpdateUser() {
        val output = updateUserViewModel.transform(
            UpdateUserViewModel.Input(
                updateUserTrigger
            )
        )

        output.response.observe(this, Observer {
            pbEditProfile?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) {
                sessionManager.storeUser(it.data.user)
                toast("Profile updated successfully")
                findNavController().popBackStack()
            }

        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbEditProfile?.visibility = View.GONE
            view?.hideKeyboard()
            handleOnApiError(it)
        })
    }


    private fun handleOnApiError(it: UIErrorResponse) {
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // this will run when coming from the image picker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {

            // Get a list of picked files
            val filesSelected = ImagePicker.getImages(data) as ArrayList<Image>
            if (filesSelected.size > 0) {

                // we'll prepare the outputfile
                val croppedImagesDir = File(
                    context?.getExternalFilesDir(null)?.absolutePath,
                    Constants.LOCAL_FOLDER_CROPPED_IMAGES
                )
                if (!croppedImagesDir.exists()) {
                    val isDirectoryCreated = croppedImagesDir.mkdir()
                }
                val fileName = Date().time.toString() + filesSelected[0].path.substringAfterLast("/")
                val outputFile = File(croppedImagesDir, fileName)

                // and then, we'll perform the crop itself
                performCrop(filesSelected[0].path, outputFile)
            }

            // this will run when coming from the cropActivity and everything is ok
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            profileHasImage = true

            Glide.with(context!!).load(resultUri).into(profile_image!!)

            //Add the photopath to recording item
            tempPhotoPath = resultUri?.path.toString()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Timber.d(cropError)
        }


        context?.let {
            //LOGIN
            if (requestCode == it.resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH) && resultCode == Activity.RESULT_OK) {
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


    private fun loadExistingData(){
        etFirstName.text = sessionManager.getStoredUser()?.first_name?.toEditable()
        etLastName.text = sessionManager.getStoredUser()?.last_name?.toEditable()
        etUsername.text = sessionManager.getStoredUser()?.username?.toEditable()
        etWebsite.text = sessionManager.getStoredUser()?.website?.toEditable()
        etBio.text = sessionManager.getStoredUser()?.description?.toEditable()
        etEmail.text = sessionManager.getStoredUser()?.email?.toEditable()
        etPhone.text = sessionManager.getStoredUser()?.phone_number?.toEditable()
        etAge.text = timestampToAge(sessionManager.getStoredUser()?.date_of_birth!!).toEditable()
        etGender.text = sessionManager.getStoredUser()?.gender?.toEditable()
        Glide.with(context!!).load(sessionManager.getStoredUser()?.images?.medium_url).into(
            profile_image!!
        )
    }


    private fun publishProfileImage() {

        if (Commons.getInstance().isImageReadyForUpload) {
            Commons.getInstance().uploadImage(
                context,
                object : Commons.ImageUploadCallback {
                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

                    override fun onSuccess(imageUrl: String?) {
                        println("Image upload to AWS succesfully")
                        //var imageUploadedUrl = imageUrl
                        profileImageUploaded = true
                        if (imageUrl != null) {
                            profileImageUrlFinal = imageUrl
                        }
                        readyToUpdate()
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {}

                    override fun onError(error: String?) {
                        profileImageUploaded = false
                        println("Image upload to AWS error: $error")
                    }
                }, Commons.IMAGE_TYPE_ATTACHMENT
            )
        } else {
            val imageFile = File(tempPhotoPath)
            Commons.getInstance().handleImage(
                context,
                Commons.IMAGE_TYPE_PODCAST,
                imageFile,
                "podcast_photo"
            )
            publishProfileImage()
        }
    }


    private fun readyToUpdate() {
        if (profileHasImage) {
            if (profileImageUploaded) {
                profileImageUploaded = false
                callToApiUpdateUser()
            }
        } else {
            callToApiUpdateUser()
        }
    }


}

