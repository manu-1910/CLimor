package com.limor.app.playlists

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.databinding.*
import com.limor.app.playlists.adapter.SelectPlaylistAdapter
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.utils.FragmentCreatePlaylist
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SaveToPlaylistFragment : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val playlistsViewModel: PlaylistsViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentSaveToPlaylistBinding

    private val podcastId: Int by lazy {
        requireArguments().getInt(
            FragmentCreatePlaylist.PODCAST_ID_KEY
        )
    }

    companion object {
        const val PODCAST_ID_KEY = "PODCAST_ID"
        val TAG = SaveToPlaylistFragment::class.qualifiedName
        fun newInstance(podcastId: Int): SaveToPlaylistFragment {
            return SaveToPlaylistFragment().apply {
                arguments = bundleOf(SaveToPlaylistFragment.PODCAST_ID_KEY to podcastId)
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
        playlistsViewModel.getPlaylistsOfCasts(podcastId).observe(viewLifecycleOwner) { playlists ->
            showPlaylists(playlists)
        }
        binding.btnCreatePlaylist.setOnClickListener {
            FragmentCreatePlaylist.newInstance(podcastId, true)
                .show(parentFragmentManager, SaveToPlaylistFragment.TAG)
            dismiss()
        }
        binding.btnDone.setOnClickListener {
            binding.btnDone.isEnabled = false
            addCast()
        }
        binding.closeImageView.setOnClickListener {
            dismiss()
        }
    }

    private fun showPlaylists(playlists: List<PlaylistUIModel?>?) {
        SelectPlaylistAdapter(onPlaylistSelected = { playlistId, selected ->
            onPlaylistSelected(playlistId, selected)
        }).also {
            val layoutManager = LinearLayoutManager(requireContext())
            binding.playlistsRv.layoutManager = layoutManager
            var list = mutableListOf<PlaylistUIModel?>()
            playlists?.let { it1 -> list.addAll(it1) }
            it.submitList(list)
            binding.playlistsRv.adapter = it
        }
    }
    
    private fun addCast() {
        playlistsViewModel.addCastToPlaylists(podcastId).observe(viewLifecycleOwner, {
            if(it.success){
                dismiss()
            } else{
                binding.btnDone.isEnabled = true
            }
        })
    }

    private fun onPlaylistSelected(playlistId: Int, selected: Boolean) {
        if (selected) {
            playlistsViewModel.playlistSelectedIdsList.add(playlistId)
        } else {
            playlistsViewModel.playlistSelectedIdsList.remove(playlistId)
        }
    }

}
