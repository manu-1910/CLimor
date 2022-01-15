package com.limor.app.scenes.utils

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.*
import androidx.core.os.bundleOf
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

    fun initialiseViews() {
        if (mode == EditMode.CREATE) {
            binding.textTitle.text = resources.getString(R.string.label_create_playlist)
            binding.btnCreate.text = resources.getString(R.string.label_create)
            binding.textInputLayout.hint = resources.getString(R.string.label_create_playlist)
        } else {
            binding.textTitle.text = resources.getString(R.string.label_rename_playlist)
            binding.btnCreate.text = resources.getString(R.string.save)
            binding.textInputLayout.hint = resources.getString(R.string.label_rename_playlist)
        }
        setCharacterMaxLength()
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.errorTV.visibility == View.VISIBLE) {
                    binding.errorTV.visibility = View.GONE
                }
            }

        })
    }

    fun setCharacterMaxLength() {
        binding.textInputLayout.counterMaxLength = 50
        binding.textInputLayout.isCounterEnabled = true
        val filters: Array<InputFilter?> = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter.LengthFilter(50)
        binding.etTitle.filters = filters
    }

}