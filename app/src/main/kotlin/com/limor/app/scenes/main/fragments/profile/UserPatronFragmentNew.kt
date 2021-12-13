package com.limor.app.scenes.main.fragments.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.SkuDetails
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import com.limor.app.dm.ui.ShareDialog
import com.limor.app.extensions.isOnline
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.casts.CastItem
import com.limor.app.scenes.main.fragments.profile.casts.LoadMoreItem
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.editpreview.EditPreviewDialog
import com.limor.app.scenes.patron.FragmentShortItemSlider
import com.limor.app.scenes.patron.manage.ManagePatronActivity
import com.limor.app.scenes.patron.manage.fragment.ChangePriceActivity
import com.limor.app.scenes.patron.setup.PatronSetupActivity
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PurchaseTarget
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import java.time.Duration
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

    private val castsAdapter = GroupieAdapter()

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
            castsAdapter.notifyItemChanged(currentCasts.size)
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
        viewModel.loadPatronCasts(user.id, Constants.CAST_BATCH_SIZE, castOffset)
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
            binding.indicator.visibility = View.GONE
        }

        binding.pager.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        setOnClicks()
        handleUIStates()

    }

    private fun subscribeToViewModel() {
        model.userProfileData.observe(viewLifecycleOwner, Observer {
            it?.let {
                user = it
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
            binding.castsList.layoutManager = LinearLayoutManager(context)
            binding.castsList.adapter = castsAdapter
            currentCasts.clear()
            if (casts.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
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
        }
    }

    private fun onPurchaseRequested(skuDetails: SkuDetails?, cast: CastUIModel) {
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

    private fun onCastClick(cast: CastUIModel, sku: SkuDetails?) {
        Timber.d("Clicked ${activity}")
        if(cast.patronDetails?.purchased == false){
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.purchase_cast_title)
                setMessage(R.string.purchase_cast_description)
                setIcon(R.drawable.ic_purchase)
                addButton(R.string.cancel, false)
                addButton(R.string.buy_now, true) {
                    launchPurchaseCast(cast, sku)
                }
            }.show()
        } else{
            (activity as? PlayerViewManager)?.showExtendedPlayer(cast.id)
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
        Timber.d("Current User state -> ${user.patronInvitationStatus} ---")
        // user.isPatron = false
        if (currentUser()) {
            if (user.isPatron == true) {
                //is already a patron
                //load patron feed
                /*binding.emptyStateLayout.visibility = View.VISIBLE
                binding.baseImageTextLayout.visibility = View.GONE
                binding.managePatronStateLayout.visibility = View.GONE
                binding.requestStateLayout.visibility = View.GONE*/
                setupViewPager(ArrayList())
                binding.audioPlayerView.visibility = View.GONE
                binding.termsCheckBox.isChecked = false
                binding.patronButton.text = getString(R.string.limorPatronSetupWallet)
                binding.patronButton.isEnabled = false
                binding.patronButton.visibility = View.GONE
                binding.managePatronStateLayout.visibility = View.VISIBLE
                binding.managePatronDescriptionTV.visibility = View.GONE
                binding.pager.visibility = View.GONE
                binding.indicator.visibility = View.INVISIBLE
                binding.checkLayout.visibility = View.INVISIBLE

                loadCasts()

                binding.managePatron.setOnClickListener {
                    val intent = Intent(requireActivity(), ManagePatronActivity::class.java)
                    startActivity(intent)
                    //findNavController().navigate(R.id.action_navigateProfileFragment_to_managePatronFragment)
                }
            } else {

                // audio should be present for all patron invitation statuses
                setupAudioPlayer(user.patronAudioURL, user.patronAudioDurationSeconds)
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
                        binding.checkLayout.visibility = View.GONE
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requested)
                    }
                    "APPROVED" -> {
                        //Approved but note yet setup
                        binding.indicator.visibility = View.GONE
                        if (user.isPatron == false) {
                            //user.patronOnBoardingStatus = "NOT_INITIATED"
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
                                "COMPLETED" -> {
                                    //Show Limor Patron
                                    setupViewPager(ArrayList())
                                    binding.audioPlayerView.visibility = View.VISIBLE
                                    binding.termsCheckBox.isChecked = false
                                    binding.patronButton.text =
                                        getString(R.string.limorPatronSetupWallet)
                                    binding.patronButton.isEnabled = true
                                    binding.managePatronStateLayout.visibility = View.VISIBLE
                                    binding.pager.visibility = View.GONE
                                    binding.indicator.visibility = View.GONE
                                    binding.checkLayout.visibility = View.GONE

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
            if (user.isPatron == true) {
                setupViewPager(ArrayList())
                binding.audioPlayerView.visibility = View.GONE
                binding.termsCheckBox.isChecked = false
                binding.patronButton.text = getString(R.string.limorPatronSetupWallet)
                binding.patronButton.isEnabled = false
                binding.patronButton.visibility = View.GONE
                binding.managePatronStateLayout.visibility = View.GONE
                binding.pager.visibility = View.GONE
                binding.indicator.visibility = View.INVISIBLE
                binding.checkLayout.visibility = View.INVISIBLE

                loadCasts()
            } else {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.baseImageTextLayout.visibility = View.GONE
                binding.managePatronStateLayout.visibility = View.GONE
                binding.requestStateLayout.visibility = View.GONE
            }

        }

    }

    private fun setNotInitiatedState() {
        setupViewPager(getNormalStateItems())
        binding.patronButton.text = getString(R.string.request_invite)
        binding.emptyStateLayout.visibility = View.GONE
        binding.baseImageTextLayout.visibility = View.VISIBLE
        binding.managePatronStateLayout.visibility = View.GONE
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
            binding.audioPlayerView.visibility = View.GONE
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

    private fun setOnClicks() {

        binding.patronButton.setOnClickListener {
            when (user.patronInvitationStatus) {
                null -> {
                    //Should request patron invitation
                    binding.patronButton.isEnabled = false
                    binding.patronButton.text = getString(R.string.requesting)
                    requestInvitation()
                }
                "NOT_REQUESTED" -> {
                    //Should request patron invitation
                    binding.patronButton.isEnabled = false
                    binding.patronButton.text = getString(R.string.requesting)
                    requestInvitation()
                }
                "APPROVED" -> checkPatronState()
                "REJECTED" -> {
                    binding.patronButton.isEnabled = false
                    binding.patronButton.text = getString(R.string.requesting)
                    requestInvitation()
                }
                "REVOKED" -> {
                    binding.patronButton.isEnabled = false
                    binding.patronButton.text = getString(R.string.requesting)
                    requestInvitation()
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
        // user.patronOnBoardingStatus = "NOT_INITIATED"
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
            "COMPLETED" -> {
                //Show Coming soon
                showSpotSecuredDialog()
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