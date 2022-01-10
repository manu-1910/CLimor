package com.limor.app.playlists

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.databinding.*
import com.limor.app.playlists.adapter.SelectPlaylistAdapter
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.utils.LimorTextInputDialog
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SaveToPlaylistFragment : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val playlistsViewModel: PlaylistsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentSaveToPlaylistBinding

    companion object {
        val TAG = SaveToPlaylistFragment::class.qualifiedName
        fun newInstance() = SaveToPlaylistFragment()
    }

    override fun getTheme() = R.style.RoundedCornersDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveToPlaylistBinding.inflate(inflater, container, false)
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
        playlistsViewModel.getPlaylists().observe(viewLifecycleOwner) { playlists ->
            showPlaylists(playlists)
        }
        binding.btnCreatePlaylist.setOnClickListener {
            LimorTextInputDialog(layoutInflater).apply {
                setTitle(R.string.label_create_playlist)
                setHint(R.string.label_playlist_name)
                addButton(R.string.cancel, false)
                addButton(R.string.label_create, true) {
                    playlistsViewModel.addToCustomPlaylist(
                        PlaylistUIModel(
                            id = 0,
                            title = getText(),
                            images = null,
                            isCustom = false,
                            count = 0,
                            selected = false
                        )
                    )
                    dismiss()
                }
            }.show()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.closeImageView.setOnClickListener {
            dismiss()
        }
    }

    private fun showPlaylists(playlists: List<PlaylistUIModel>) {
        SelectPlaylistAdapter().also {
            val layoutManager = LinearLayoutManager(requireContext())
            binding.playlistsRv.layoutManager = layoutManager
            var list = mutableListOf<PlaylistUIModel>()
            list.addAll(playlists)
            list.addAll(playlists)
            it.submitList(list)
            binding.playlistsRv.adapter = it
        }
    }

}