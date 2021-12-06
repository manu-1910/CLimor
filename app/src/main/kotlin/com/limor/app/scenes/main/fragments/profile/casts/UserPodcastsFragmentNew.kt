package com.limor.app.scenes.main.fragments.profile.casts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.android.billingclient.api.ConsumeParams
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentUserCastsBinding
import com.limor.app.di.Injectable
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.PlayBillingHandler
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserPodcastsFragmentNew : Fragment(), Injectable {

    companion object {
        private const val USER_ID_KEY = "USER_ID_KEY"
        fun newInstance(user: UserUIModel) = UserPodcastsFragmentNew().apply {
            arguments = bundleOf(USER_ID_KEY to user)
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
    @Inject
    lateinit var playBillingHandler: PlayBillingHandler
    private val user: UserUIModel by lazy { requireArguments().getParcelable(USER_ID_KEY)!! }

    private var castOffset = 0
    private var sharedPodcastId = -1

    private val castsAdapter = GroupieAdapter()

    private val currentCasts = mutableListOf<CastUIModel>()
    private val loadMoreItem = LoadMoreItem() {
        updateLoadMore(false)
        onLoadMore()
    }

    var launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (sharedPodcastId != -1) {
                sharePodcastViewModel.share(sharedPodcastId)
                sharedPodcastId = -1
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
        binding.btnRecordPodcast.setOnClickListener {
            findNavController().navigate(R.id.navigation_record)
        }
    }

    private fun onLoadMore() {
        castOffset = currentCasts.size
        loadCasts()
    }

    private fun reloadCurrentCasts() {
        val loadedCount = currentCasts.size
        castOffset = 0
        viewModel.loadCasts(user.id, loadedCount, castOffset)
    }

    private fun subscribeForEvents() {
        viewModel.casts.observe(viewLifecycleOwner) { casts ->
            onLoadCasts(casts)
        }
        recastPodcastViewModel.recastedResponse.observe(viewLifecycleOwner) {
            reloadCurrentCasts()
        }
        recastPodcastViewModel.deleteRecastResponse.observe(viewLifecycleOwner) {
            reloadCurrentCasts()
        }
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner) {
            reloadCurrentCasts()
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
                onShareClick = { cast, onShared ->
                    ShareDialog.newInstance(cast).also { fragment ->
                        fragment.setOnSharedListener(onShared)
                        fragment.show(parentFragmentManager, fragment.requireTag())
                    }
                },
                onHashTagClick = { hashtag ->
                    (activity as? PlayerViewManager)?.navigateToHashTag(hashtag)
                },
                onPurchaseCast = {cast, sku ->
                    sku?.let { skuDetails ->
                        playBillingHandler.launchBillingFlowFor(skuDetails, requireActivity()){ purchase ->
                            lifecycleScope.launch {
                                playBillingHandler.consumePurchase(ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken).build())
                                val response  = playBillingHandler.publishRepository.createCastPurchase(cast,purchase,skuDetails)
                                if(response == "Success"){
                                    //reload cast item for now reloading all items
                                    loadCasts()
                                }
                            }
                        }
                    }
                },
                productDetailsFetcher = playBillingHandler
            )
        }
    }

    private fun onLoadCasts(casts: List<CastUIModel>) {
        if (castOffset == 0) {
            currentCasts.clear()
            lifecycleScope.launch {
                JwtChecker.getUserIdFromJwt(false)?.let {
                    if (user.id == it) {
                        if (casts.isEmpty()) {
                            binding.noPodcastsLayout.visibility = View.VISIBLE
                        }

                    } else {
                        binding.noPodcastsLayout.visibility = View.GONE
                    }
                }
            }



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
        val needNotification = isEnabled != loadMoreItem.isEnabled
        loadMoreItem.isEnabled = isEnabled
        if (needNotification) {
            // notify the last item (i.e. the LoadMoreItem) has changes so its style is updated.
            castsAdapter.notifyItemChanged(currentCasts.size)
        }
    }

    private fun reload() {
        castOffset = 0
        loadCasts()
    }

    private fun loadCasts() {
        Timber.d("Profile Casts Loading for ${user.id}")
        viewModel.loadCasts(user.id, Constants.CAST_BATCH_SIZE, castOffset)
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

    private fun scrollList(){
        binding.castsList.scrollBy(0, 10)
    }

}
