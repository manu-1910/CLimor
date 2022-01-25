package com.limor.app.scenes.utils

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.databinding.FragmentCreatePlaylistBinding
import com.limor.app.playlists.PlaylistsViewModel
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


enum class EditMode {
    CREATE, RENAME
}
data class PlaylistResult(
    val editMode: EditMode,
    val success: Boolean,
    val title: String
)

class FragmentCreatePlaylist : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PlaylistsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentCreatePlaylistBinding

    var onResult: ((result: PlaylistResult) -> Unit)? = null

    private val podcastId: Int by lazy {
        requireArguments().getInt(PODCAST_ID_KEY)
    }

    private val playlistId: Int by lazy {
        requireArguments().getInt(PLAYLIST_ID_KEY)
    }

    private var mode = EditMode.CREATE

    companion object {
        val TAG = FragmentCreatePlaylist::class.qualifiedName
        const val PODCAST_ID_KEY = "PODCAST_ID"
        const val PLAYLIST_ID_KEY = "PLAYLIST_ID"
        const val USE_CREATE_MODE = "MODE"

        fun editPlaylist(playlistId: Int): FragmentCreatePlaylist {
            return FragmentCreatePlaylist().apply {
                arguments = bundleOf(PLAYLIST_ID_KEY to playlistId, USE_CREATE_MODE to false)
            }
        }

        fun createPlaylist(podcastId: Int): FragmentCreatePlaylist {
            return FragmentCreatePlaylist().apply {
                arguments = bundleOf(PODCAST_ID_KEY to podcastId, USE_CREATE_MODE to true)
            }
        }
    }

    override fun getTheme() = R.style.RoundedCornersDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)

        mode = if (requireArguments().getBoolean(USE_CREATE_MODE, false)) {
            EditMode.CREATE
        } else {
            EditMode.RENAME
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViews()
        binding.btnCreate.setOnClickListener {
            onMainAction()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun onMainAction() {
        if (binding.etTitle.text.isNullOrEmpty()) {
            displayError(getString(R.string.plalyst_title_length_error))
            return
        }

        if (mode == EditMode.CREATE) {
            createPlaylist()
        } else {
            editPlaylist()
        }
    }

    private fun displayError(errorMessage: String?) {
        val error = errorMessage?.split("-> ")?.lastOrNull() ?: errorMessage
        binding.errorTV.apply {
            text = error
            visibility = View.VISIBLE
        }
    }

    private fun editPlaylist() {
        val title = binding.etTitle.text.toString()
        model.editPlaylist(title, playlistId).observe(viewLifecycleOwner, {
            if (it.success) {
                onResult?.invoke(PlaylistResult(
                    editMode = mode,
                    success = true,
                    title = title
                ))
                dismiss()
            } else {
                displayError(it.error)
            }
        })
    }

    fun createPlaylist() {
        model.createPlaylist(binding.etTitle.text.toString(), podcastId).observe(viewLifecycleOwner, {
            if (it.success) {
                dismiss()
            } else {
                displayError(it.error)
            }
        })
    }

    private fun initialiseViews() {
        if (mode == EditMode.CREATE) {
            binding.textTitle.text = getString(R.string.label_create_playlist)
            binding.btnCreate.text = getString(R.string.label_create)
            binding.textInputLayout.hint = getString(R.string.label_playlist_name)
        } else {
            binding.textTitle.text = getString(R.string.label_rename_playlist)
            binding.btnCreate.text = getString(R.string.save)
            binding.textInputLayout.hint = getString(R.string.label_rename_playlist)
        }
        setCharacterMaxLength()
        binding.etTitle.doAfterTextChanged {
            if (it.toString().matches(Regex("[a-zA-Z0-9]*[\"/@#$%^&_+=()`~!*\':;/,.<>?'|{}-]+[a-zA-Z0-9]*"))) {
                binding.errorTV.text = getString(R.string.special_characters_not_allowed)
                binding.errorTV.visibility = View.VISIBLE
                binding.btnCreate.isEnabled = false
            } else {
                binding.errorTV.visibility = View.GONE
                binding.btnCreate.isEnabled = (it?.length ?: 0) > 0
            }
        }
    }

    private fun setCharacterMaxLength() {
        binding.textInputLayout.apply {
            counterMaxLength = 50
            isCounterEnabled = true
        }

        binding.etTitle.filters = arrayOf(InputFilter.LengthFilter(50))
    }

}