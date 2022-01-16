package com.limor.app.scenes.utils

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.databinding.FragmentCreatePlaylistBinding
import com.limor.app.playlists.PlaylistsViewModel
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FragmentCreatePlaylist : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PlaylistsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentCreatePlaylistBinding

    private val podcastId: Int by lazy {
        requireArguments().getInt(
            FragmentCreatePlaylist.PODCAST_ID_KEY
        )
    }

    private var mode = EditMode.CREATE

    enum class EditMode {
        CREATE, RENAME
    }

    companion object {
        val TAG = FragmentCreatePlaylist::class.qualifiedName
        const val PODCAST_ID_KEY = "PODCAST_ID"
        const val MODE = "MODE"
        fun newInstance(podcastId: Int, create: Boolean): FragmentCreatePlaylist {
            return FragmentCreatePlaylist().apply {
                arguments = bundleOf(PODCAST_ID_KEY to podcastId, MODE to create)
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
    ): View? {
        binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        mode = if (requireArguments().getBoolean(
                FragmentCreatePlaylist.MODE, false
            )
        ) {
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
            createPlaylist()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    fun createPlaylist(){
        model.createPlaylist(binding.etTitle.text.toString(), podcastId).observe(viewLifecycleOwner, {
            if(!it.success){
                val error = it.error?.split("-> ")
                if(error?.size != null && error.size >= 2){
                    binding.errorTV.text = error[1]
                } else {
                    binding.errorTV.text = it.error
                }
                binding.errorTV.visibility = View.VISIBLE
            } else{
                dismiss()
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
            if (binding.errorTV.visibility == View.VISIBLE) {
                binding.errorTV.visibility = View.GONE
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