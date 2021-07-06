package com.limor.app.scenes.main.fragments.profile.casts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.databinding.FragmentUserCastsBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter
import javax.inject.Inject

class UserPodcastsFragmentNew : Fragment(), Injectable {

    companion object {
        private const val USER_ID_KEY = "USER_ID_KEY"
        fun newInstance(userId: Int) = UserPodcastsFragmentNew().apply {
            arguments = bundleOf(USER_ID_KEY to userId)
        }
    }

    private var _binding: FragmentUserCastsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: UserPodcastsViewModel by viewModels { viewModelFactory }

    private val userId: Int by lazy { requireArguments().getInt(USER_ID_KEY) }

    private val castsAdapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserCastsBinding.inflate(inflater)
        viewModel.loadCasts(userId)

        initViews()
        subscribeForEvents()
        return binding.root
    }

    private fun initViews() {
        binding.castsList.layoutManager = LinearLayoutManager(context)
        binding.castsList.adapter = castsAdapter
    }

    private fun subscribeForEvents() {
        viewModel.casts.observe(viewLifecycleOwner) { casts ->
            castsAdapter.update(
                casts.map {
                    CastItem(
                        cast = it,
                        onCastClick = ::onCastClick,
                        onLikeClick = { cast, like -> viewModel.likeCast(cast, like) },
                        onMoreDialogClick = ::onMoreDialogClick,
                    )
                }
            )
        }
    }

    private fun onCastClick(cast: CastUIModel) {
        // TODO
    }

    private fun onMoreDialogClick(cast: CastUIModel) {
        DialogPodcastMoreActions.newInstance(castId = cast.id)
            .show(parentFragmentManager, DialogPodcastMoreActions.TAG)
    }
}