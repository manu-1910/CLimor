package com.limor.app.scenes.main.fragments.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.SkuDetails
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentPurchasesBinding
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.casts.CastItem
import com.limor.app.scenes.main.fragments.profile.casts.LoadMoreItem
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.editpreview.EditPreviewDialog
import com.limor.app.scenes.patron.manage.fragment.ChangePriceActivity
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PurchaseTarget
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToAudioTrack
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FragmentPurchases(var user: UserUIModel) : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserPodcastsViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentPurchasesBinding

    @Inject
    lateinit var playBillingHandler: PlayBillingHandler

    private var castOffset = 0
    private var sharedPodcastId = -1

    private val castsAdapter = GroupieAdapter()

    private val currentCasts = mutableListOf<CastUIModel>()
    private val loadMoreItem = LoadMoreItem {
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
                    type = "text/plain"
                }
                sharedPodcastId = cast.id
                val shareIntent = Intent.createChooser(sendIntent, null)
                launcher.launch(shareIntent)
            } catch (e: ActivityNotFoundException) {
            }

        }.addOnFailureListener {
            Timber.d("Failed in creating short dynamic link")
        }

    }

    companion object {
        fun newInstance(user: UserUIModel) = FragmentPurchases(user)
    }

    private fun updateLoadMore(isEnabled: Boolean) {
        val needNotification = isEnabled != loadMoreItem.isEnabled
        loadMoreItem.isEnabled = isEnabled
        if (needNotification) {
            // notify the last item (i.e. the LoadMoreItem) has changes so its style is updated.
            castsAdapter.notifyItemChanged(currentCasts.size)
        }
    }

    private fun onLoadMore() {
        castOffset = currentCasts.size
        loadCasts()
    }

    private fun loadCasts() {
        Timber.d("Patron Casts Loading for ${user.id}")
        model.loadPurchasedCasts(user.id, Constants.CAST_BATCH_SIZE, castOffset)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchasesBinding.inflate(inflater, container, false)
        subscribeToViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCasts()
    }

    private fun subscribeToViewModel() {
        model.purchasedCasts.observe(viewLifecycleOwner) { casts ->
            onLoadCasts(casts)
        }
    }

    private fun onLoadCasts(casts: List<CastUIModel>) {
        if (castOffset == 0) {
            binding.castsList.layoutManager = LinearLayoutManager(context)
            binding.castsList.adapter = castsAdapter
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

    private fun getCastItems(casts: List<CastUIModel>): List<CastItem> {
        return casts.map {
            CastItem(
                cast = it,
                onCastClick = ::onCastClick,
                onLikeClick = { cast, like -> model.likeCast(cast, like) },
                onMoreDialogClick = ::onMoreDialogClick,
                onRecastClick = { cast, isRecasted ->
                    if (isRecasted) {
                        recastPodcastViewModel.reCast(cast.id)
                    } else {
                        recastPodcastViewModel.deleteRecast(cast.id)
                    }
                },
                onCommentsClick = { cast, skuDetails ->
                    onCommentClick(cast, skuDetails)
                },
                onShareClick = { cast, onShared ->
                    ShareDialog.newInstance(it).also { fragment ->
                        fragment.setOnSharedListener(onShared)
                        fragment.show(parentFragmentManager, fragment.requireTag())
                    }
                },
                onHashTagClick = { hashtag ->
                    (activity as? PlayerViewManager)?.navigateToHashTag(hashtag)
                },
                isPurchased = true,
                onPurchaseCast = { cast, sku ->

                },
                onEditPreviewClick = {
                },
                onPlayPreviewClick = { cast, play ->
                },
                onEditPriceClick = { cast ->
                }
            )
        }
    }

    private fun onCommentClick(cast: CastUIModel, sku: SkuDetails?){
        if(cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(requireContext())) {
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.purchase_cast_title)
                setMessage(R.string.purchase_cast_description_for_comment)
                setIcon(R.drawable.ic_comment_purchase)
                addButton(R.string.cancel, false)
                addButton(R.string.buy_now, true) {
                    launchPurchaseCast(cast, sku)
                }
            }.show()
        } else{
            RootCommentsFragment.newInstance(cast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }
    }

    private fun launchPurchaseCast(cast: CastUIModel, skuDetails: SkuDetails?) {
        val sku = skuDetails ?: return
        val purchaseTarget = PurchaseTarget(sku, cast)
        playBillingHandler.launchBillingFlowFor(purchaseTarget, requireActivity()) { success ->
            if (success) {
                lifecycleScope.launch {
                    reload()
                }
            }
        }
    }

    private fun onCastClick(cast: CastUIModel, skuDetails: SkuDetails?) {
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

    private fun reload() {
        castOffset = 0
        loadCasts()
    }

}