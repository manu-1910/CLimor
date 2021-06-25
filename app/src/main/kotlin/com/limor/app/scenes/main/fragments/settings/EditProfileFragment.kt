package com.limor.app.scenes.main.fragments.settings


import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.snackbar.Snackbar
import com.limor.app.App
import com.limor.app.GetUserProfileQuery
import com.limor.app.R
import com.yalantis.ucrop.UCrop
import io.reactivex.subjects.PublishSubject
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.databinding.FragmentEditBinding
import com.limor.app.databinding.FragmentEditProfileBinding
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.showSnackbar
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.CommonsKt.Companion.toEditable
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIUser
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

    private var currentUser: GetUserProfileQuery.GetUser? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val  model: SettingsViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var updateUserViewModel: UpdateUserViewModel
    private val updateUserTrigger = PublishSubject.create<Unit>()

    private lateinit var binding: FragmentEditProfileBinding
    var app: App? = null
    var profileImageUploaded = true
    var profileHasImage = false
    var profileImageUrlFinal = ""
    var tempPhotoPath = ""
    var user: UIUser? = null


    companion object {
        val TAG: String = EditProfileFragment::class.java.simpleName
        fun newInstance() = EditProfileFragment()
        const val TIMBER_TAG = "BIRTHDATE_ISSUE"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        app = context?.applicationContext as App
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = sessionManager.getStoredUser()

        //Setup animation transition
      //  ViewCompat.setTranslationZ(view, 1f)

        loadExistingData()
        /*bindViewModel()
        loadExistingData()
        apiCallUpdateUser()*/
        addClickListeners()
        fetchRequiredData()
    }

    private fun addClickListeners() {
        binding.btnUpdate.setOnClickListener {
            updateUserData()
        }

        binding.btnChoosePhoto.setOnClickListener {
           // loadImagePicker()

        }


    }

    private fun fetchRequiredData() {

        lifecycleScope.launchWhenCreated {
            currentUser  = model.getUserInfo()
            bindUserDataToViews()
        }

    }


    private fun updateUserData(){

        lifecycleScope.launchWhenCreated {

            val response = model.updateUserInfo(
                binding.etUsernameInner.text.toString(),
                binding.etFirstNameInner.text.toString(),
                binding.etLastNameInner.text.toString(),
                binding.etBioInner.text.toString(),
                binding.etWebUrlInner.text.toString()
            )

            response?.let{
                binding.root.showSnackbar(it,Snackbar.LENGTH_SHORT)
                (activity)?.finish()
            }

            bindUserDataToViews()

        }

    }

    private fun bindUserDataToViews() {
        currentUser?.let{

            binding.etUsernameInner.setText(it.username)
            binding.etFirstNameInner.setText(it.first_name)
            binding.etLastNameInner.setText(it.last_name)
            binding.etWebUrlInner.setText(it.website)
            binding.etBioInner.setText(it.description)


        }
    }


    private fun bindViewModel() {

    }



    private fun loadImagePicker() {
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


    private fun callToApiUpdateUser(){

    }


    private fun apiCallUpdateUser() {

    }


    private fun handleOnApiError(it: UIErrorResponse) {

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        /*if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {


        }*/
        // this will run when coming from the image picker
       /* if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {

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
        super.onActivityResult(requestCode, resultCode, data)*/
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

      /*  etFirstName.text = user?.first_name?.toEditable()
        etLastName.text = user?.last_name?.toEditable()
        etUsername.text = user?.username?.toEditable()
        etWebsite.text = user?.website?.toEditable()
        etBio.text = user?.description?.toEditable()
        etEmail.text = user?.email?.toEditable()
        etPhone.text = user?.phone_number?.toEditable()

        etAge.setText("")
        user?.date_of_birth?.let {
            etAge.setText(CommonsKt.calculateAge(it).toString())
        }
        etGender.text = user?.gender?.toEditable()
        Glide.with(context!!).load(user?.images?.medium_url).into(
            profile_image!!
        )*/
    }


    private fun publishProfileImage() {

        /*if (Commons.getInstance().isImageReadyForUpload) {
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
        }*/
    }


    private fun readyToUpdate() {
       /* if (profileHasImage) {
            if (profileImageUploaded) {
                profileImageUploaded = false
                callToApiUpdateUser()
            }
        } else {
            callToApiUpdateUser()
        }*/
    }


}

