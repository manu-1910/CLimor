package com.limor.app.playlists

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentShareDialogBinding
import com.limor.app.databinding.PlaylistsFragmentBinding
import com.limor.app.playlists.adapter.PlaylistsAdapter
import com.limor.app.playlists.models.PlaylistUIModel
import com.limor.app.scenes.patron.FragmentPlaylistDetails
import com.limor.app.uimodels.UserUIModel
import javax.inject.Inject

class PlaylistsFragment : BaseFragment() {

    private var _binding: PlaylistsFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val playlistsViewModel: PlaylistsViewModel by viewModels { viewModelFactory }

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
        playlistsViewModel.getPlaylists().observe(viewLifecycleOwner) { playlists ->
            showPlaylists(playlists)
        }
    }

    private fun showPlaylists(playlists: List<PlaylistUIModel>) {
        PlaylistsAdapter(
            onPlaylistClick = { playlist ->
                val args = Bundle()
                args.putBoolean(FragmentPlaylistDetails.IS_PLAYLIST, playlist?.isCustom ?: false)
                args.putString(FragmentPlaylistDetails.LIST_NAME, playlist?.title)
                findNavController().navigate(R.id.action_navigateProfileFragment_to_fragmentPlaylistDetails, args)
            },
            onDeleteClick = { playlist ->

            }
        ).also {
            it.submitList(playlists)
            binding.recyclerPlaylists.adapter = it
        }
    }

    companion object {
        private const val KEY_USER = "KEY_USER"
        fun newInstance(user: UserUIModel) = PlaylistsFragment().apply {
            arguments = bundleOf(KEY_USER to user)
        }
    }
}