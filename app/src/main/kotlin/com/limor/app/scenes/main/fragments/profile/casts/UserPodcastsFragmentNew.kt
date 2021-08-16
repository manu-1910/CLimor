package com.limor.app.scenes.main.fragments.profile.casts

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentUserCastsBinding
import com.limor.app.di.Injectable
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber
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
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }
    private val podcastInteractionViewModel: PodcastInteractionViewModel by activityViewModels { viewModelFactory }

    private val userId: Int by lazy { requireArguments().getInt(USER_ID_KEY) }

    private var castOffset = 0

    private val castsAdapter = GroupieAdapter()

    private val currentCasts = mutableListOf<CastUIModel>()
    private val loadMoreItem = LoadMoreItem() {
        updateLoadMore(false)
        onLoadMore()
    }

    var launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val podcastId = intent?.getIntExtra(Constants.SHARED_PODCAST_ID, -1) ?: -1
                if (podcastId != -1) {
                    sharePodcastViewModel.share(podcastId)
                }
            }
        }

    val sharePodcast: (CastUIModel) -> Unit = { cast ->

        val podcastLink = Constants.PODCAST_URL.format(cast.id)

        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(podcastLink)
            domainUriPrefix = Constants.LIMOR_DOMAIN_URL
            androidParameters(BuildConfig.APPLICATION_ID) {
                fallbackUrl = Uri.parse(podcastLink)
            }
            iosParameters(BuildConfig.IOS_BUNDLE_ID) {
            }
            socialMetaTagParameters {
                title = cast.title.toString()
                description = cast.caption.toString()
                cast.imageLinks?.large?.let {
                    imageUrl = Uri.parse(cast.imageLinks.large)
                }
            }
        }

        Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { (shortLink, flowChartLink) ->
            try {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, cast.title)
                    putExtra(Intent.EXTRA_TEXT, "Hey, check out this podcast: $shortLink")
                    putExtra(Constants.SHARED_PODCAST_ID, cast.id)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                launcher.launch(shareIntent)
            } catch (e: ActivityNotFoundException) {
            }

        }.addOnFailureListener {
            Timber.d("Failed in creating short dynamic link")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserCastsBinding.inflate(inflater)
        initViews()
        subscribeForEvents()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCasts()
    }

    private fun initViews() {
        binding.castsList.layoutManager = LinearLayoutManager(context)
        binding.castsList.adapter = castsAdapter
    }

    private fun onLoadMore() {
        castOffset = currentCasts.size
        loadCasts()
    }

    private fun subscribeForEvents() {
        viewModel.casts.observe(viewLifecycleOwner) { casts ->
            onLoadCasts(casts)
        }
        recastPodcastViewModel.recastedResponse.observe(viewLifecycleOwner) {
            viewModel.loadCasts(userId)
        }
        recastPodcastViewModel.deleteRecastResponse.observe(viewLifecycleOwner) {
            viewModel.loadCasts(userId)
        }
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner) {
            viewModel.loadCasts(userId)
        }
        podcastInteractionViewModel.reload.observe(viewLifecycleOwner) {
            reload()
        }
    }

    private fun getCastItems(casts: List<CastUIModel>): List<CastItem> {
        return casts.map {
            CastItem(
                cast = it,
                onCastClick = ::onCastClick,
                onLikeClick = { cast, like -> viewModel.likeCast(cast, like) },
                onMoreDialogClick = ::onMoreDialogClick,
                onRecastClick = { cast, isRecasted ->
                    if (isRecasted) {
                        recastPodcastViewModel.reCast(cast.id)
                    } else {
                        recastPodcastViewModel.deleteRecast(cast.id)
                    }
                },
                onCommentsClick = { cast ->
                    RootCommentsFragment.newInstance(cast).also { fragment ->
                        fragment.show(parentFragmentManager, fragment.requireTag())
                    }
                },
                onShareClick = {
                    sharePodcast(it)
                }
            )
        }
    }

    private fun onLoadCasts(casts: List<CastUIModel>) {
        if (castOffset == 0) {
            currentCasts.clear()
        }

        currentCasts.addAll(casts)

        val items = getCastItems(currentCasts)
        val all = mutableListOf<BindableItem<out ViewBinding>>()
        all.addAll(items)
        if (currentCasts.size >= Constants.CAST_BATCH_SIZE && casts.size >= Constants.CAST_BATCH_SIZE) {
            all.add(loadMoreItem)
        }

        val recyclerViewState = binding.castsList.layoutManager?.onSaveInstanceState()
        castsAdapter.update(all)
        updateLoadMore(true)
        binding.castsList.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun updateLoadMore(isEnabled: Boolean) {
        loadMoreItem.isEnabled = isEnabled
        // notify the last item (i.e. the LoadMoreItem) has changes so its style is updated.
        castsAdapter.notifyItemChanged(currentCasts.size)
    }

    private fun reload() {
        castOffset = 0
        loadCasts()
    }

    private fun loadCasts() {
        viewModel.loadCasts(userId, Constants.CAST_BATCH_SIZE, castOffset)
    }

    private fun onCastClick(cast: CastUIModel) {
        Timber.d("Clicked ${activity}")
        (activity as? PlayerViewManager)?.showExtendedPlayer(cast.id)
    }

    private fun onMoreDialogClick(cast: CastUIModel) {
        val bundle = bundleOf(DialogPodcastMoreActions.CAST_KEY to cast)
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("reload_feed")
            ?.observe(
                viewLifecycleOwner
            ) {
                reload()
            }
        navController.navigate(R.id.dialog_report_podcast, bundle)
    }
}
