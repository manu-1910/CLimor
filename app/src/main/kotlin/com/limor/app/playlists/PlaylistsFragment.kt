package com.limor.app.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.PlaylistsFragmentBinding
import com.limor.app.playlists.adapter.PlaylistsAdapter
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.patron.FragmentPlaylistDetails
import com.limor.app.uimodels.UserUIModel
import javax.inject.Inject

class PlaylistsFragment : BaseFragment() {

    private var _binding: PlaylistsFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val playlistsViewModel: PlaylistsViewModel by viewModels { viewModelFactory }

    private lateinit var playlistAdapter: PlaylistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PlaylistsFragmentBinding.inflate(inflater, container, false);
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPlaylists()
    }

    private fun loadPlaylists(){
        playlistsViewModel.getPlaylists().observe(viewLifecycleOwner) { playlists ->
            showPlaylists(playlists)
        }
    }

    private fun showPlaylists(playlists: List<PlaylistUIModel?>?) {
        val result = playlists?.groupBy({it?.isCustom}, {it})
        val sortedResult = mutableListOf<PlaylistUIModel?>()
        result?.get(false)?.let { sortedResult.addAll(it) }
        result?.get(true)?.let { sortedResult.addAll(it) }
        playlistAdapter = PlaylistsAdapter(
            onPlaylistClick = { playlist ->
                if(playlist.count > 0){
                    findNavController().navigate(
                        R.id.action_navigateProfileFragment_to_fragmentPlaylistDetails,
                        bundleOf(FragmentPlaylistDetails.KEY_PLAYLIST to playlist)
                    )
                    if(activity is MainActivityNew){
                        (activity as MainActivityNew).ensureLayout()
                    }
                }
            },
            onDeleteClick = { playlist ->
                deletePlaylist(playlist.id)
            }
        ).also {
            it.submitList(sortedResult)
            binding.recyclerPlaylists.adapter = it
        }
    }

    private fun deletePlaylist(playlistId: Int){
        playlistsViewModel.deletePlaylist(playlistId).observe(viewLifecycleOwner, {
            if(it.success){
                loadPlaylists()
            }
        })
    }

    companion object {
        private const val KEY_USER = "KEY_USER"
        fun newInstance(user: UserUIModel) = PlaylistsFragment().apply {
            arguments = bundleOf(KEY_USER to user)
        }
    }
}