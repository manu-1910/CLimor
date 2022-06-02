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
import com.android.billingclient.api.ProductDetails
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentPurchasesBinding
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.casts.LoadMoreItem
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.adapters.CastsAdapter
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PurchaseTarget
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToAudioTrack
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collectLatest
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

    private var castsAdapter: CastsAdapter? = null

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
            castsAdapter?.notifyItemChanged(currentCasts.size)
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        subscribeToViewModel()
        loadCasts()
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            model.getPatronCasts(user.id).collectLatest { data ->
                castsAdapter?.submitData(data)
                /*if (castsAdapter?.snapshot().size == 0) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }*/
            }
        }
    }

    private fun setAdapter(){
        castsAdapter = CastsAdapter(
            userId = user.id,
            onCastClick = ::onCastClick,
            onLikeClick = { cast, like ->
                if (like) {
                    Sounds.playSound(requireContext(), SoundType.HEART)
                }
                model.likeCast(cast, like)
            },
            onMoreDialogClick = ::onMoreDialogClick,
            onRecastClick = { cast, isRecasted ->
                if (isRecasted) {
                    Sounds.playSound(requireContext(), SoundType.RECAST)
                    recastPodcastViewModel.reCast(cast.id)
                } else {
                    recastPodcastViewModel.deleteRecast(cast.id)
                }
            },
            onCommentsClick = { cast, skuDetails ->
                onCommentClick(cast, skuDetails)
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
            isPurchased = true,
            onPurchaseCast = { cast, sku ->

            },
            onEditPreviewClick = {
            },
            onPlayPreviewClick = { cast, play ->
                cast.audio?.mapToAudioTrack()?.let { it1 ->
                    cast.patronDetails?.startsAt?.let { it2 ->
                        cast.patronDetails.endsAt?.let { it3 ->
                            if (play) {
                                (activity as? PlayerViewManager)?.playPreview(
                                    it1, it2.toInt(), it3.toInt()
                                )
                            } else {
                                (activity as? PlayerViewManager)?.stopPreview(true)
                            }
                        }
                    }
                }
            },
            onEditPriceClick = { cast ->
            }
        )
    }

    private fun onCommentClick(cast: CastUIModel, product: ProductDetails?){
        if(cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(requireContext())) {
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.purchase_cast_title)
                setMessage(R.string.purchase_cast_description_for_comment)
                setIcon(R.drawable.ic_comment_purchase)
                addButton(R.string.cancel, false)
                addButton(R.string.buy_now, true) {
                    launchPurchaseCast(cast, product)
                }
            }.show()
        } else{
            RootCommentsFragment.newInstance(cast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }
    }

    private fun launchPurchaseCast(cast: CastUIModel, skuDetails: ProductDetails?) {
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

    private fun onCastClick(cast: CastUIModel, skuDetails: ProductDetails?) {
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