package com.limor.app.playlists

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.limor.app.R
import com.limor.app.uimodels.UserUIModel
import javax.inject.Inject

class PlaylistsFragment : Fragment() {

    companion object {
        private const val KEY_USER = "KEY_USER"
        fun newInstance(user: UserUIModel) = PlaylistsFragment().apply {
            arguments = bundleOf(KEY_USER to user)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val playlistsViewModel: PlaylistsViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.playlists_fragment, container, false)
    }

}