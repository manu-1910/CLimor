package com.limor.app.scenes.main.fragments.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.android.billingclient.api.ProductDetails
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.isOnline
import com.limor.app.extensions.requireTag
import com.limor.app.extensions.visibleIf
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.casts.LoadMoreItem
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.adapters.CastsAdapter
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.editpreview.EditPreviewDialog
import com.limor.app.scenes.patron.FragmentShortItemSlider
import com.limor.app.scenes.patron.manage.ManagePatronActivity
import com.limor.app.scenes.patron.manage.fragment.ChangePriceActivity
import com.limor.app.scenes.patron.setup.PatronSetupActivity
import com.limor.app.scenes.patron.unipaas.UniPaasActivity
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PurchaseTarget
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToAudioTrack
import com.limor.app.util.SoundType
import com.limor.app.util.Sounds
import com.xwray.groupie.viewbinding.BindableItem
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject


class UserPatronFragmentNew : Fragment() {

    companion object {
        private const val USER_ID_KEY = "USER_ID_KEY"
        fun newInstance(user: UserUIModel) = UserPatronFragmentNew().apply {
            arguments = bundleOf(USER_ID_KEY to user)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserProfileViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var playBillingHandler: PlayBillingHandler
    private lateinit var user: UserUIModel

    private val viewModel: UserPodcastsViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmnetUserPatronNewBinding
    var requested = false
    private var castOffset = 0
    private var sharedPodcastId = -1

    private var castsAdapter: CastsAdapter? = null

    var editPriceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                reload()
            }
        }

    private val currentCasts = mutableListOf<CastUIModel>()
    private val loadMoreItem = LoadMoreItem {
        updateLoadMore(false)
        onLoadMore()
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
        if (BuildConfig.DEBUG) {
            Timber.d("Patron Casts Loading for ${user.id}")
        }
        lifecycleScope.launch {
            viewModel.getPatronCasts(user.id).collectLatest { data ->
                if(isActive){
                    castsAdapter?.submitData(data)
                }
            }
        }
        castsAdapter?.addLoadStateListener { it ->
            if (it.source.append.endOfPaginationReached) {
                binding.emptyStateLayout.visibleIf(castsAdapter?.snapshot()?.isEmpty() ?: true)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = requireArguments().getParcelable(USER_ID_KEY)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmnetUserPatronNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupViewPager(items: ArrayList<FragmentShortItemSlider>) {
        binding.pager.adapter = ShortPagerAdapter(items, childFragmentManager, lifecycle)
        if (items.size > 1) {
            binding.indicator.setViewPager2(binding.pager)
            binding.indicator.visibility = View.VISIBLE
        } else {
            binding.indicator.visibility = GONE
        }

        binding.pager.visibility = if (items.isEmpty()) GONE else View.VISIBLE


    }

    private fun getNormalStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_1_title,
            R.drawable.patron_carousel_slide_1_image,
            R.string.patron_carousel_slide_1_sub_title,
        )
        val item2 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_2_title,
            R.drawable.patron_carousel_slide_2_image,
            R.string.patron_carousel_slide_2_sub_title,
        )
        val item3 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_3_title,
            R.drawable.patron_carousel_slide_3_image,
            R.string.patron_carousel_slide_3_sub_title,
        )
        return arrayListOf(item1, item2, item3)
    }

    private fun getApprovedStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_approved_title,
            R.drawable.ic_patron_invite_accepted,
            0
        )
        return arrayListOf(item1)
    }


    private fun getPurchasedStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(
            R.string.patron_complete_on_boarding_title,
            R.drawable.patron_carousel_slide_3_image,
            R.string.patron_membership_purchased
        )
        return arrayListOf(item1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        subscribeToViewModel()
        setOnClicks()

        if (user.id != PrefsHandler.getCurrentUserId(requireContext())) {
            handleUIStates()
        }
    }

    private fun subscribeToViewModel() {
        model.userProfileData.observe(viewLifecycleOwner, Observer {
            it?.let {
                user = it
                if (BuildConfig.DEBUG) {
                    println("Got user -> $it")
                }
                handleUIStates()
            }
        })

        viewModel.patronCasts.observe(viewLifecycleOwner) { casts ->
            onLoadCasts(casts)
        }
    }

    private fun onLoadCasts(casts: List<CastUIModel>) {
        if (BuildConfig.DEBUG) {
            Timber.d("Got ${casts.size} patron casts.")
        }
        if (castOffset == 0) {
            currentCasts.clear()
            if (casts.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
            }
        }

        currentCasts.addAll(casts)

        //val items = getCastItems(currentCasts)
        val all = mutableListOf<BindableItem<out ViewBinding>>()
        //all.addAll(items)
        if (currentCasts.size >= Constants.CAST_BATCH_SIZE && casts.size >= Constants.CAST_BATCH_SIZE) {
            all.add(loadMoreItem)
        }

        val recyclerViewState = binding.castsList.layoutManager?.onSaveInstanceState()
        //castsAdapter.update(all)
        //updateLoadMore(true)
        binding.castsList.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun setAdapter(){
        castsAdapter = CastsAdapter(
            userId = user.id,
            onCastClick = ::onCastClick,
            onLikeClick = { cast, like ->
                if (like) {
                    Sounds.playSound(requireContext(), SoundType.HEART)
                }
                viewModel.likeCast(cast, like)
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
            onPurchaseCast = { cast, sku ->
                onPurchaseRequested(sku, cast)
            },
            productDetailsFetcher = playBillingHandler,
            onEditPreviewClick = { cast ->
                EditPreviewDialog.newInstance(cast).also { fragment ->
                    fragment.show(parentFragmentManager, fragment.requireTag())
                }
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
                Intent(requireActivity(), ChangePriceActivity::class.java).apply {
                    putExtra(ChangePriceActivity.CHANGE_PRICE_FOR_ALL_CASTS, false)
                    putExtra(ChangePriceActivity.CAST_ID, cast.id)
                    putExtra(ChangePriceActivity.SELECTED_PRICE_ID, cast.patronDetails?.priceId)
                }.also {
                    editPriceLauncher.launch(it)
                }

            }
        )
        binding.castsList.layoutManager = LinearLayoutManager(context)
        binding.castsList.adapter = castsAdapter
    }
    
    private fun onPurchaseRequested(skuDetails: ProductDetails?, cast: CastUIModel) {
        val sku = skuDetails ?: return
        val purchaseTarget = PurchaseTarget(sku, cast)
        playBillingHandler.launchBillingFlowFor(purchaseTarget, requireActivity()) { success ->
            if (success) {
                lifecycleScope.launch {
                    loadCasts()
                }
            }
        }
    }

    private fun onCommentClick(cast: CastUIModel, sku: ProductDetails?) {
        if (cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(
                requireContext())
        ) {
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.purchase_cast_title)
                setMessage(R.string.purchase_cast_description_for_comment)
                setIcon(R.drawable.ic_comment_purchase)
                addButton(R.string.cancel, false)
                addButton(R.string.buy_now, true) {
                    launchPurchaseCast(cast, sku)
                }
            }.show()
        } else {
            RootCommentsFragment.newInstance(cast).also { fragment ->
                fragment.show(parentFragmentManager, fragment.requireTag())
            }
        }
    }

    private fun onCastClick(cast: CastUIModel, sku: ProductDetails?) {
        Timber.d("Clicked ${activity}")
        if(cast.patronDetails?.purchased == false && cast.owner?.id != PrefsHandler.getCurrentUserId(requireContext())) {
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.purchase_cast_title)
                setMessage(R.string.purchase_cast_description)
                setIcon(R.drawable.ic_purchase)
                addButton(R.string.cancel, false)
                addButton(R.string.buy_now, true) {
                    launchPurchaseCast(cast, sku)
                }
            }.show()
        } else {
            (activity as? PlayerViewManager)?.showExtendedPlayer(cast.id)
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


    override fun onResume() {
        super.onResume()
        if (currentUser()) {
            Timber.d("Get user")
            model.getUserProfile()
        }
    }

    private fun currentUser(): Boolean {

        Timber.d(
            "Current User Check -> ${user.id} --- ${
                PrefsHandler.getCurrentUserId(requireContext())
            }"
        )
        return when (user.id) {
            PrefsHandler.getCurrentUserId(requireContext()) -> {
                //Current user
                true
            }
            else -> {
                //Other user
                false
            }
        }
    }

    private fun handleUIStates() {
        binding.emptyStateTv.text = if (currentUser()) {
            getString(R.string.limor_patron_empty_state)
        } else getString(R.string.patron_empty_state_other)
        val result: Spanned = HtmlCompat.fromHtml(
            getString(R.string.patron_uk_account_learn_more),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.termsTV.text = result
        binding.termsTV.movementMethod = LinkMovementMethod.getInstance()
        binding.termsCheckBox.isChecked = false
        if (BuildConfig.DEBUG) {
            Timber.d("Current User (${user.username}) state -> ${user.patronInvitationStatus} ---> ${user.patronOnBoardingStatus}. Is Patron -> ${user.isPatron}")
        }
        // user.isPatron = false
        if (currentUser()) {
            if (user.isPatron == true) {
                //user.patronOnBoardingStatus = "ACTION_REQUIRED"
                if(user.patronOnBoardingStatus == "ACTION_REQUIRED"){
                    setupAudioPlayer(user.patronAudioURL, user.patronAudioDurationSeconds)
                    setupViewPager(ArrayList())
                    binding.audioPlayerView.visibility = View.VISIBLE
                    binding.termsCheckBox.isChecked = false
                    binding.patronButton.text =
                        if (PrefsHandler.hasOnboardingUrl(requireContext()))
                            getString(R.string.complete_onboarding) else
                            getString(R.string.limorPatronSetupWallet)

                    binding.patronButton.isEnabled = true
                    binding.managePatronStateLayout.visibility = View.VISIBLE
                    binding.managePatronDescriptionTV.visibility = View.VISIBLE
                    binding.pager.visibility = GONE
                    binding.indicator.visibility = GONE
                    binding.checkLayout.visibility = GONE

                    binding.managePatron.setOnClickListener {
                        val intent = Intent(
                            requireActivity(),
                            ManagePatronActivity::class.java
                        )
                        startActivity(intent)
                    }
                } else{
                    //is already a patron
                    //load patron feed
                    /*binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.baseImageTextLayout.visibility = View.GONE
                    binding.managePatronStateLayout.visibility = View.GONE
                    binding.requestStateLayout.visibility = View.GONE*/
                    setupViewPager(ArrayList())
                    binding.audioPlayerView.visibility = GONE
                    binding.termsCheckBox.isChecked = false
                    binding.patronButton.text = getString(R.string.limorPatronSetupWallet)
                    binding.patronButton.isEnabled = false
                    binding.patronButton.visibility = GONE
                    binding.managePatronStateLayout.visibility = View.VISIBLE
                    binding.managePatronDescriptionTV.visibility = GONE
                    binding.pager.visibility = GONE
                    binding.indicator.visibility = View.INVISIBLE
                    binding.checkLayout.visibility = View.INVISIBLE

                    loadCasts()

                    binding.managePatron.setOnClickListener {
                        val intent = Intent(requireActivity(), ManagePatronActivity::class.java)
                        startActivity(intent)
                        //findNavController().navigate(R.id.action_navigateProfileFragment_to_managePatronFragment)
                    }
                }
            } else {
                // user.patronOnBoardingStatus = "NOT_INITIATED"
                // audio should be present for all patron invitation statuses
                if(user.patronOnBoardingStatus!="MEMBERSHIP_PURCHASED"){
                    setupAudioPlayer(user.patronAudioURL, user.patronAudioDurationSeconds)
                }
                // user.patronInvitationStatus = "APPROVED"
                when (user.patronInvitationStatus) {
                    null -> {
                        //Considering this as NOT_REQUESTED STATE
                        setNotInitiatedState()
                    }
                    "NOT_REQUESTED" -> {
                        //Show Request Invite state
                        setNotInitiatedState()
                    }
                    "REQUESTED" -> {
                        setupViewPager(getNormalStateItems())
                        binding.checkLayout.visibility = GONE
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requested)
                    }
                    "APPROVED" -> {
                        //Approved but note yet setup
                        binding.indicator.visibility = GONE
                        if (user.isPatron == false) {

                            when (user.patronOnBoardingStatus) {
                                null -> {
                                    setupViewPager(getApprovedStateItems())
                                    binding.checkLayout.visibility = View.VISIBLE
                                    binding.patronButton.text = getString(R.string.limorPatronSetup)
                                }
                                "NOT_INITIATED" -> {
                                    setupViewPager(getApprovedStateItems())
                                    binding.checkLayout.visibility = View.VISIBLE
                                    binding.patronButton.text = getString(R.string.limorPatronSetup)
                                }

                                "MEMBERSHIP_PURCHASED" -> {
                                    setupViewPager(getPurchasedStateItems())
                                    binding.checkLayout.visibility = GONE
                                    binding.patronButton.text = getString(R.string.continue_button)
                                    binding.patronButton.isEnabled = true
                                }

                                "CATEGORIES_COLLECTED" -> {
                                    setupViewPager(getPurchasedStateItems())
                                    binding.checkLayout.visibility = View.GONE
                                    binding.patronButton.text = getString(R.string.continue_button)
                                    binding.patronButton.isEnabled = true
                                    binding.audioPlayerView.visibility = View.GONE
                                }
                                "LANGUAGES_COLLECTED" -> {
                                    setupViewPager(ArrayList())
                                    binding.audioPlayerView.visibility = View.VISIBLE
                                    binding.termsCheckBox.isChecked = false
                                    binding.patronButton.text = getString(R.string.limorPatronSetupWallet)

                                    binding.patronButton.isEnabled = true
                                    binding.managePatronStateLayout.visibility = View.VISIBLE
                                    binding.managePatronDescriptionTV.visibility = View.VISIBLE
                                    binding.pager.visibility = GONE
                                    binding.indicator.visibility = GONE
                                    binding.checkLayout.visibility = GONE

                                    binding.managePatron.setOnClickListener {
                                        val intent = Intent(
                                            requireActivity(),
                                            ManagePatronActivity::class.java
                                        )
                                        startActivity(intent)
                                    }
                                }
                                "COMPLETED" -> {
                                }
                                "VENDOR_CREATED" -> {
                                    setupViewPager(ArrayList())
                                    binding.audioPlayerView.visibility = View.VISIBLE
                                    binding.termsCheckBox.isChecked = false
                                    binding.patronButton.text =
                                        getString(R.string.complete_onboarding)
                                    binding.patronButton.isEnabled = true
                                    binding.managePatronStateLayout.visibility = View.VISIBLE
                                    binding.pager.visibility = GONE
                                    binding.indicator.visibility = GONE
                                    binding.checkLayout.visibility = GONE

                                    binding.managePatron.setOnClickListener {
                                        val intent = Intent(
                                            requireActivity(),
                                            ManagePatronActivity::class.java
                                        )
                                        startActivity(intent)
                                    }
                                }
                                "ACCEPT_PAYMENTS" -> {

                                }
                                "ACTION_REQUIRED" -> {

                                }
                                "BANK_DETAILS_PENDING" ->{
                                    setupViewPager(ArrayList())
                                    binding.audioPlayerView.visibility = View.VISIBLE
                                    binding.termsCheckBox.isChecked = false
                                    binding.patronButton.text =
                                        getString(R.string.digital_wallet_processing)
                                    binding.patronButton.isEnabled = true
                                    binding.managePatronStateLayout.visibility = View.VISIBLE
                                    binding.pager.visibility = GONE
                                    binding.indicator.visibility = GONE
                                    binding.checkLayout.visibility = GONE
                                    binding.patronButton.isEnabled = false
                                    binding.managePatron.setOnClickListener {
                                        val intent = Intent(
                                            requireActivity(),
                                            ManagePatronActivity::class.java
                                        )
                                        startActivity(intent)
                                    }
                                }
                                else -> {
                                    setupViewPager(getApprovedStateItems())
                                    binding.patronButton.text = getString(R.string.limorPatronSetup)
                                    binding.checkLayout.visibility = View.VISIBLE
                                    binding.patronButton.isEnabled = false
                                }

                            }
                        }

                    }
                    "REJECTED" -> {
                        setNotInitiatedState()
                    }
                    "REVOKED" -> {
                        setNotInitiatedState()
                    }
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                println("Current user (${user.username} is not me and isPatron -> ${user.isPatron}")
            }
            if (user.isPatron == true) {
                setupViewPager(ArrayList())
                binding.audioPlayerView.visibility = GONE
                binding.termsCheckBox.isChecked = false
                binding.patronButton.text = getString(R.string.limorPatronSetupWallet)
                binding.patronButton.isEnabled = false
                binding.patronButton.visibility = GONE
                binding.managePatronStateLayout.visibility = GONE
                binding.pager.visibility = GONE
                binding.indicator.visibility = View.INVISIBLE
                binding.checkLayout.visibility = View.INVISIBLE

                loadCasts()
            } else {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.baseImageTextLayout.visibility = GONE
                binding.managePatronStateLayout.visibility = GONE
                binding.requestStateLayout.visibility = GONE
            }

        }

        if(binding.progress.isVisible){
            binding.progress.visibility = GONE
        }


    }

    private fun setNotInitiatedState() {
        setupViewPager(getNormalStateItems())
        binding.patronButton.text = getString(R.string.request_invite)
        binding.emptyStateLayout.visibility = GONE
        binding.baseImageTextLayout.visibility = View.VISIBLE
        binding.managePatronStateLayout.visibility = GONE
        binding.requestStateLayout.visibility = View.VISIBLE
        binding.checkLayout.visibility = View.VISIBLE
        binding.patronButton.isEnabled = false
        binding.termsCheckBox.isChecked = false
        subscribeToInvite()
    }

    private fun subscribeToInvite() {
        model.patronInviteStatus.observe(viewLifecycleOwner, { inviteStatus ->
            if (inviteStatus == "Success") {
                user.patronInvitationStatus = "REQUESTED"
                handleUIStates()
            } else {
                binding.root.snackbar(getString(R.string.patron_invite_not_required))
                user.patronInvitationStatus = "NOT_REQUESTED"
                handleUIStates()
            }
        })
    }

    private fun setupAudioPlayer(url: String?, durationSeconds: Double?) {
        Timber.d("$url ---- AUDIO")
        if (url.isNullOrEmpty()) {
            binding.audioPlayerView.visibility = GONE
        } else {
            val durationMillis = ((durationSeconds ?: 0.0) * 1000.0).toLong()
            binding.audioPlayer.initialize(
                AudioCommentUIModel(
                    url = url,
                    duration = Duration.ofMillis(durationMillis)
                )
            )
            binding.audioPlayerView.visibility = View.VISIBLE
        }
    }

    private fun requestInvitation() {
        if (requireContext().isOnline()) {
            model.requestPatronInvitation(user.id)
        } else {
            binding.root.snackbar(getString(R.string.default_no_internet))
        }
    }

    private fun isEligibleForPatronRequest() : Boolean{
        if (ChronoUnit.YEARS.between(user.dateOfBirth, LocalDate.now()) < 18) {
            LimorDialog(layoutInflater).apply {
                setIcon(R.drawable.ic_alert)
                setTitle(R.string.age_restriction_title)
                setMessage(R.string.patron_request_age_restrinction_message)
                addButton(android.R.string.ok, true)
            }.show()
            return false
        }
        return true
    }

    private fun setOnClicks() {

        binding.patronButton.setOnClickListener {
            when (user.patronInvitationStatus) {
                null -> {
                    //Should request patron invitation
                    if(isEligibleForPatronRequest()){
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requesting)
                        requestInvitation()
                    }
                }
                "NOT_REQUESTED" -> {
                    //Should request patron invitation
                    if(isEligibleForPatronRequest()){
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requesting)
                        requestInvitation()
                    }
                }
                "APPROVED" -> checkPatronState()
                "REJECTED" -> {
                    if(isEligibleForPatronRequest()){
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requesting)
                        requestInvitation()
                    }
                }
                "REVOKED" -> {
                    if(isEligibleForPatronRequest()){
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requesting)
                        requestInvitation()
                    }
                }

            }
            //Should setup Limor patron
        }

        binding.termsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.patronButton.isEnabled = isChecked
        }

        binding.termsTV.setOnClickListener { binding.termsCheckBox.performClick() }
    }

    private fun checkPatronState() {
        //user.patronOnBoardingStatus = "NOT_INITIATED"
        when (user.patronOnBoardingStatus) {
            "NOT_INITIATED" -> {
                val intent = Intent(requireContext(), PatronSetupActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }
            "MEMBERSHIP_PURCHASED" -> {
                //Go to Categories
                val intent = Intent(requireContext(), PatronSetupActivity::class.java)
                intent.putExtra("user", user)
                intent.putExtra("page", "categories")
                startActivity(intent)

            }
            "CATEGORIES_COLLECTED" -> {
                //Go to Languages
                val intent = Intent(requireContext(), PatronSetupActivity::class.java)
                intent.putExtra("user", user)
                intent.putExtra("page", "languages")
                startActivity(intent)
            }
            "LANGUAGES_COLLECTED" -> {
                val intent = Intent(requireContext(), UniPaasActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }
            "COMPLETED" -> {
            }
            "VENDOR_CREATED" -> {
                model.getVendorOnBoardingUrl().observe(viewLifecycleOwner) {
                    it?.let {
                        PrefsHandler.setOnboardingUrl(requireContext(), it)
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        startActivity(browserIntent)
                    }
                }
            }
            "ACCEPT_PAYMENTS" -> {

            }
            "ACTION_REQUIRED" -> {
                if(user.isPatron == true){
                    model.getVendorOnBoardingUrl().observe(viewLifecycleOwner) {
                        it?.let {
                            PrefsHandler.setOnboardingUrl(requireContext(), it)
                            val intent = Intent(requireContext(), UniPaasActivity::class.java)
                            intent.putExtra("user", user)
                            intent.putExtra("show_confirmation", true)
                            startActivity(intent)
                        }
                    }
                }
            }
        }

    }

    private fun showSpotSecuredDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_spot_secured, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog: AlertDialog = dialogBuilder.create()
        dialogView.okButton.setOnClickListener {
            dialog.dismiss()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }


}