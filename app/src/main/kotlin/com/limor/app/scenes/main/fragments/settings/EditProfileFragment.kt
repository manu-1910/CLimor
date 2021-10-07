package com.limor.app.scenes.main.fragments.settings


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.model.Image
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.common.SessionManager
import com.limor.app.databinding.FragmentEditProfileBinding
import com.limor.app.extensions.drawSimpleSelectorDialog
import com.limor.app.scenes.main.fragments.record.PublishFragment
import com.limor.app.scenes.main.fragments.record.RecordFragment
import com.limor.app.scenes.main.viewmodels.UpdateUserViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.VoiceBioEvent
import com.limor.app.scenes.utils.VoiceBioInfo
import com.limor.app.uimodels.UIUser
import com.limor.app.uimodels.UserUIModel
import com.limor.app.util.*
import com.yalantis.ucrop.UCrop
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


class EditProfileFragment : BaseFragment(), Commons.AudioUploadCallback {

    private val storagePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                loadImagePicker()
            }
        }

    private lateinit var currentUser: UIUserUpdateModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: SettingsViewModel by viewModels({ activity as SettingsActivity }) { viewModelFactory }

    private var mCaptureFromCamera = false;

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var updateUserViewModel: UpdateUserViewModel
    private val updateUserTrigger = PublishSubject.create<Unit>()

    private lateinit var binding: FragmentEditProfileBinding
    var app: App? = null
    var voiceBioUploaded = false
    var profileImageUploaded = false
    var profileHasImage = false
    var profileImageUrlFinal = ""
    var tempPhotoPath = ""
    var user: UIUser? = null

    var hasChangedVoiceBio = false;
    var newBioPath: String? = null
    var newBioDurationSeconds: Double? = null

    companion object {
        val TAG: String = EditProfileFragment::class.java.simpleName
        const val CAMERA_REQUEST = 512
        const val CAMERA_PERMISSION_REQUEST = 700
        fun newInstance() = EditProfileFragment()
        const val TIMBER_TAG = "BIRTHDATE_ISSUE"
    }

    private var selectedPhotoFile: File? = null

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

        model.userUpdatedResponse.observe(viewLifecycleOwner) {
            when (it) {
                SettingsViewModel.USER_UPDATE_SUCCESS -> {
                    SettingsActivity.finishWithResult(activity, true)
                }
                SettingsViewModel.USER_UPDATE_FAILURE -> {
                    reportError(R.string.could_not_update_profile)
                    hideLoading()
                }
            }
        }

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
            showImageOptions()
        }
    }

    private fun showImageOptions() {
        drawSimpleSelectorDialog(
            getString(R.string.select_image_option_title),
            listOf(getString(R.string.take_a_new_photo_option), getString(R.string.choose_from_gallery_option))
        )
        { dialog: DialogInterface, i: Int ->
            mCaptureFromCamera = i == 0
            loadImagePicker()
            dialog.dismiss()
        }
    }

    private fun fetchRequiredData() {
        model.getUserInfo()
    }

    private fun listenForVoiceBioEvents() {
        binding.voiceBio.onVoiceBioEvents() {
            when (it) {
                is VoiceBioEvent.NewVoiceBio -> {
                    hasChangedVoiceBio = true

                    newBioPath = it.path
                    newBioDurationSeconds = it.durationSeconds

                    ensureNullableVoiceBio()

                    toggleVoiceLabel(it.path)
                }
                else -> {
                    // Do nothing...
                }
            }
        }
    }

    private fun ensureNullableVoiceBio() {
        if (newBioPath == null && hasChangedVoiceBio) {
            currentUser.voiceBioURL = null
            currentUser.durationSeconds = null
        }
    }

    private fun toggleVoiceLabel(source: String?) {
        binding.tvRecordVoiceBio.setText(
            if (source.isNullOrEmpty())
                R.string.voice_bio_label_no_bio
            else
                R.string.voice_bio_label_with_bio
        )
    }

    private fun bindUserDataToViews() {
        currentUser.let {
            binding.etUsernameInner.setText(it.userName)
            binding.etFirstNameInner.setText(it.firstName)
            binding.etLastNameInner.setText(it.lastName)
            binding.etWebUrlInner.setText(it.website)
            binding.etBioInner.setText(it.bio)

            it.voiceBioURL?.let { voiceBioUrl ->
                binding.voiceBio.voiceBioInfo = VoiceBioInfo(voiceBioUrl, it.durationSeconds ?: 0.0)
            }

            toggleVoiceLabel(it.voiceBioURL)
            listenForVoiceBioEvents()

            Glide.with(requireContext())
                .load(it.imageURL)
                .signature(ObjectKey("${it.imageURL}"))
                .error(R.drawable.ic_podcast_listening)
                .into(binding.profileImage)
        }
    }

    private fun checkPermissionForCamera(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForCamera() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
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

    private fun loadImagePicker() {
        if (!hasStoragePermissions(requireContext())) {
            storagePermissionsLauncher.launch(STORAGE_PERMISSIONS)
            return
        }

        if (mCaptureFromCamera) {
            captureImage();
            return;
        }

        ImagePicker.create(this) // Activity or Fragment
            .showCamera(true) // show camera or not (true by default)
            .folderMode(true) // folder mode (false by default)
            .toolbarFolderTitle(getString(R.string.imagepicker_folder)) // folder selection title
            .toolbarImageTitle(getString(R.string.imagepicker_tap_to_select)) // image selection title
            .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
            .includeVideo(false) // Show video on image picker
            .limit(resources.getInteger(R.integer.MAX_PHOTOS)) // max images can be selected (99 by default)
            .theme(R.style.ImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
            .single()
            .start()
    }

    private fun callToApiUpdateUser() {
        ensureNullableVoiceBio()

        model.updateUserInfo(
            binding.etUsernameInner.text.toString(),
            binding.etFirstNameInner.text.toString(),
            binding.etLastNameInner.text.toString(),
            binding.etBioInner.text.toString(),
            binding.etWebUrlInner.text.toString(),
            currentUser.imageURL,
            currentUser.voiceBioURL,
            currentUser.durationSeconds
        )
    }

    private fun prepareToCrop(sourcePath: String) {
        val croppedImagesDir = File(
            context?.getExternalFilesDir(null)?.absolutePath,
            Constants.LOCAL_FOLDER_CROPPED_IMAGES
        )
        if (!croppedImagesDir.exists()) {
            val isDirectoryCreated = croppedImagesDir.mkdirs()
        }
        val fileName =
            Date().time.toString() + sourcePath.substringAfterLast("/")
        val outputFile = File(croppedImagesDir, fileName)

        // and then, we'll perform the crop itself
        performCrop(sourcePath, outputFile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST) {
            selectedPhotoFile?.let {
                prepareToCrop(it.path)
            }
            // this will run when coming from the cropActivity and everything is ok
        } else if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {

            // Get a list of picked files
            val filesSelected = ImagePicker.getImages(data) as ArrayList<Image>
            if (filesSelected.size > 0) {

                // we'll prepare the outputfile
                val croppedImagesDir = File(
                    context?.getExternalFilesDir(null)?.absolutePath,
                    Constants.LOCAL_FOLDER_CROPPED_IMAGES
                )
                if (!croppedImagesDir.exists()) {
                    val isDirectoryCreated = croppedImagesDir.mkdirs()
                }
                val fileName =
                    Date().time.toString() + filesSelected[0].path.substringAfterLast("/")
                val outputFile = File(croppedImagesDir, fileName)

                // and then, we'll perform the crop itself
                performCrop(filesSelected[0].path, outputFile)
            }

            // this will run when coming from the cropActivity and everything is ok
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            data.let {
                // There sometimes seems to be a delay until the image file is available to read
                // so we try to mitigate this by delaying accessing it
                binding.profileImage.postDelayed({
                    onCropResult(it)
                }, 500)
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Timber.d(cropError)
            reportError(getString(R.string.crop_error_message__with_format, cropError));
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onCropResult(data: Intent?) {
        if (null == data) {
            reportError(R.string.image_processing_failed_generic_message)
            Timber.d("Cropping failed, no data.")
            return
        }

        val resultUri = UCrop.getOutput(data)
        if (null == resultUri) {
            reportError(R.string.image_processing_failed_generic_message)
            Timber.d("Cropping failed, could not get output.")
            return
        }

        val path = resultUri.path.toString()
        if (!File(path).exists()) {
            reportError(R.string.image_processing_failed_generic_message)
            Timber.d("Cropping failed, resulting file path ($path) does not exist.")
            return
        }

        profileHasImage = true
        Glide.with(requireContext()).load(resultUri).into(binding.profileImage)
        //Add the photopath to recording item
        tempPhotoPath = resultUri.path.toString()
    }


    private fun performCrop(sourcePath: String, destination: File) {
        // (/storage/emulated/0/Android/data/com.limor.app.staging/files/limorv2/cropped-images/1631190554648im3age.png) does not exist.
        val sourceUri = Uri.fromFile(File(sourcePath))
        val destinationUri = Uri.fromFile(destination)
        context?.let {
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1.0f, 1.0f)
                .withMaxResultSize(1000, 1000)
                .start(it, this, UCrop.REQUEST_CROP)
        }
    }

    private fun uploadVoiceBio() {
        val path = newBioPath
        if (path.isNullOrEmpty()) {
            return
        }

        Commons.getInstance().uploadAudio(
            context,
            File(path),
            Constants.AUDIO_TYPE_VOICE_BIO,
            this
        )
    }

    private fun publishProfileImage() {

        if (Commons.getInstance().isImageReadyForUpload) {
            Commons.getInstance().uploadImage(
                context,
                object : Commons.ImageUploadCallback {
                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

                    override fun onSuccess(imageUrl: String?) {
                        Timber.d("Image upload to Firebase storage successfully")
                        //var imageUploadedUrl = imageUrl
                        profileImageUploaded = true
                        if (imageUrl != null) {
                            profileImageUrlFinal = imageUrl
                            currentUser.imageURL = profileImageUrlFinal
                        }
                        println("URL: $imageUrl")
                        readyToUpdate()
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {}

                    override fun onError(error: String?) {
                        profileImageUploaded = false
                        reportError(R.string.image_upload_failed_generic_message)
                        hideLoading()
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

    private fun reportError(strResId: Int) {
        binding.root.snackbar(strResId)
    }

    private fun reportError(text: String) {
        binding.root.snackbar(text)
    }

    private fun readyToUpdate() {

        if(binding.etFirstNameInner.text.isNullOrEmpty()){
            binding.etFirstNameInner.error = "Required"
            binding.etFirstNameInner.requestFocus()
            return
        }
        if(binding.etLastNameInner.text.isNullOrEmpty()){
            binding.etLastNameInner.error = "Required"
            binding.etLastNameInner.requestFocus()
            return
        }
        if(binding.etUsernameInner.text.isNullOrEmpty()){
            binding.etUsernameInner.error = "Required"
            binding.etUsernameInner.requestFocus()
            return
        }
        showLoading()

        if (!newBioPath.isNullOrBlank() && !voiceBioUploaded) {
            uploadVoiceBio()
        } else if (profileHasImage) {
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
        var imageURL: String?,
        var voiceBioURL: String?,
        var durationSeconds: Double?
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
                        user.imageLinks?.original,
                        user.voiceBioURL,
                        user.durationSeconds
                    )
                }
            }
        }
    }

    override fun onSuccess(audioUrl: String?) {
        // voice bio uploaded
        currentUser.voiceBioURL = audioUrl
        currentUser.durationSeconds = newBioDurationSeconds
        voiceBioUploaded = true
        readyToUpdate()
    }

    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
        // TODO show voice bio upload progress?
    }

    override fun onError(error: String?) {
        Timber.d("Voice bio audio upload to AWS error: $error")
        reportError(R.string.voice_bio_upload_fail_message)
        hideLoading()
    }
}
