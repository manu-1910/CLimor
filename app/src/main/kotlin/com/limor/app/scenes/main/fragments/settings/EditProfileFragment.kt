package com.limor.app.scenes.main.fragments.settings


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.snackbar.Snackbar
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.databinding.FragmentEditProfileBinding
import com.limor.app.extensions.showSnackbar
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.uimodels.UIErrorResponse
import com.limor.app.uimodels.UIUser
import com.limor.app.uimodels.UserUIModel
import com.yalantis.ucrop.UCrop
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


class EditProfileFragment : BaseFragment() {

    private lateinit var currentUser: UIUserUpdateModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: SettingsViewModel by viewModels({ activity as SettingsActivity }) { viewModelFactory }


    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var updateUserViewModel: UpdateUserViewModel
    private val updateUserTrigger = PublishSubject.create<Unit>()

    private lateinit var binding: FragmentEditProfileBinding
    var app: App? = null
    var profileImageUploaded = false
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
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        app = context?.applicationContext as App
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = sessionManager.getStoredUser()

        configureToolbar()
        addViewModelOrbservers()
        addClickListeners()
        fetchRequiredData()
    }

    private fun addViewModelOrbservers() {
        model.userInfoLiveData.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                currentUser = UIUserUpdateModel.createFrom(it)
                bindUserDataToViews()
                hideLoading()
            }
        })

        model.userUpdatedResponse.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.root.showSnackbar(it, Snackbar.LENGTH_SHORT)
            }
            hideLoading()
        })

    }

    private fun hideLoading() {
        binding.loading.visibility = View.GONE
    }

    private fun showLoading() {
        binding.loading.visibility = View.VISIBLE
    }

    private fun configureToolbar() {
        model.setToolbarTitle(resources.getString(R.string.edit_profile))
    }

    private fun addClickListeners() {
        binding.btnUpdate.setOnClickListener {
            readyToUpdate()
        }

        binding.btnChoosePhoto.setOnClickListener {
            loadImagePicker()

        }


    }

    private fun fetchRequiredData() {
        model.getUserInfo()
    }


    private fun updateUserData() {

    }

    private fun bindUserDataToViews() {
        currentUser.let {
            binding.etUsernameInner.setText(it.userName)
            binding.etFirstNameInner.setText(it.firstName)
            binding.etLastNameInner.setText(it.lastName)
            binding.etWebUrlInner.setText(it.website)
            binding.etBioInner.setText(it.bio)
            Glide.with(requireContext()).load(it.imageURL)
                .error(R.drawable.limor_orange_primary)
                .into(binding.profileImage)
        }
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


    private fun callToApiUpdateUser() {
        model.updateUserInfo(
            binding.etUsernameInner.text.toString(),
            binding.etFirstNameInner.text.toString(),
            binding.etLastNameInner.text.toString(),
            binding.etBioInner.text.toString(),
            binding.etWebUrlInner.text.toString(),
            currentUser.imageURL
        )

    }


    private fun apiCallUpdateUser() {

    }


    private fun handleOnApiError(it: UIErrorResponse) {

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

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
                val fileName =
                    Date().time.toString() + filesSelected[0].path.substringAfterLast("/")
                val outputFile = File(croppedImagesDir, fileName)

                // and then, we'll perform the crop itself
                performCrop(filesSelected[0].path, outputFile)
            }

            // this will run when coming from the cropActivity and everything is ok
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            profileHasImage = true
            Glide.with(requireContext()).load(resultUri).into(binding.profileImage)
            //Add the photopath to recording item
            tempPhotoPath = resultUri?.path.toString()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Timber.d(cropError)
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


    private fun publishProfileImage() {

        if (Commons.getInstance().isImageReadyForUpload) {
            Commons.getInstance().uploadImage(
                context,
                object : Commons.ImageUploadCallback {
                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

                    override fun onSuccess(imageUrl: String?) {
                        Timber.d("Image upload to AWS successfully")
                        //var imageUploadedUrl = imageUrl
                        profileImageUploaded = true
                        if (imageUrl != null) {
                            profileImageUrlFinal = imageUrl
                            currentUser.imageURL = profileImageUrlFinal
                        }
                        readyToUpdate()
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {}

                    override fun onError(error: String?) {
                        profileImageUploaded = false
                        binding.root.snackbar("Image upload Failed. Try again")
                        Timber.d("Image upload to AWS error: $error")
                    }
                }, Commons.IMAGE_TYPE_PROFILE
            )
        } else {
            val imageFile = File(tempPhotoPath)
            Commons.getInstance().handleImage(
                context,
                Commons.IMAGE_TYPE_PROFILE,
                imageFile,
                "podcast_photo"
            )
            publishProfileImage()
        }
    }


    private fun readyToUpdate() {
        showLoading()
        if (profileHasImage) {
            if (profileImageUploaded) {
                profileImageUploaded = false
                callToApiUpdateUser()
            } else {
                publishProfileImage()
            }
            Timber.d("Image $profileImageUploaded")
        } else {
            Timber.d("No Image")
            callToApiUpdateUser()
        }
    }


    data class UIUserUpdateModel(
        var userName: String?,
        var firstName: String?,
        var lastName: String?,
        var bio: String?,
        var website: String?,
        var imageURL: String?
    ) {
        companion object {
            fun createFrom(it: UserUIModel): UIUserUpdateModel {
                return it.let { user ->
                    UIUserUpdateModel(
                        user.username,
                        user.firstName,
                        user.lastName,
                        user.description,
                        user.website,
                        user.imageLinks.small
                    )
                }
            }
        }
    }


}

