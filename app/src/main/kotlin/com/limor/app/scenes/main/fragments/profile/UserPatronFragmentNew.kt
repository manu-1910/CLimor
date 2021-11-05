package com.limor.app.scenes.main.fragments.profile

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
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import com.limor.app.extensions.isOnline
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.patron.FragmentShortItemSlider
import com.limor.app.scenes.patron.setup.PatronSetupActivity
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.UserUIModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject


class UserPatronFragmentNew(var user: UserUIModel) : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UserProfileViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmnetUserPatronNewBinding
    var requested = false

    companion object {
        fun newInstance(user: UserUIModel) = UserPatronFragmentNew(user)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
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
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getNormalStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_1_title,
            R.drawable.patron_carousel_slide_1_image
        )
        val item2 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_2_title,
            R.drawable.patron_carousel_slide_2_image
        )
        val item3 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_3_title,
            R.drawable.patron_carousel_slide_3_image
        )
        return arrayListOf(item1, item2, item3)
    }

    private fun getApprovedStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(
            R.string.patron_carousel_slide_approved_title,
            R.drawable.ic_patron_invite_accepted
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
    }

    override fun onStart() {
        super.onStart()
        if(currentUser()){
            model.getUserProfile()
        }
    }

    private fun currentUser(): Boolean {

        Timber.d("Current User Check -> ${user.id} --- ${
            PrefsHandler.getCurrentUserId(requireContext())
        }")
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
        val result: Spanned = HtmlCompat.fromHtml(getString(R.string.patron_uk_account_learn_more),
            HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.termsTV.text = result
        binding.termsTV.movementMethod = LinkMovementMethod.getInstance()
        Timber.d("Current User state -> ${user.patronInvitationStatus} ---")

        if (currentUser()) {
            if (user.isPatron == true) {
                //is already a patron
                //load patron feed
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.baseImageTextLayout.visibility = View.GONE
                binding.managePatronStateLayout.visibility = View.GONE
                binding.requestStateLayout.visibility = View.GONE


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
                        if (user.isPatron == false) {
                            //user.patronOnBoardingStatus = "NOT_INITIATED"
                            when (user.patronOnBoardingStatus) {
                                null -> {
                                    setupViewPager(getApprovedStateItems())
                                    binding.patronButton.text = getString(R.string.limorPatronSetup)
                                }
                                "NOT_INITIATED" -> {
                                    setupViewPager(getApprovedStateItems())
                                    binding.checkLayout.visibility = View.VISIBLE
                                    binding.patronButton.text = getString(R.string.limorPatronSetup)
                                }
                                "COMPLETED" -> {
                                    //Show Limor Patron
                                    //setupViewPager(getNormalStateItems())
                                    binding.patronButton.text =
                                        getString(R.string.limorPatronSetupWallet)
                                    binding.patronButton.isEnabled = true
                                    binding.pager.visibility = View.GONE
                                    binding.indicator.visibility = View.GONE
                                    binding.checkLayout.visibility = View.GONE
                                    binding.managePatronStateLayout.visibility = View.VISIBLE
                                }
                                else -> {
                                    setupViewPager(getApprovedStateItems())
                                    binding.patronButton.text = getString(R.string.limorPatronSetup)
                                    binding.patronButton.isEnabled = true
                                }

                            }
                        }

                    }
                    "REJECTED" -> {
                        setupViewPager(getNormalStateItems())
                        binding.patronButton.isEnabled = false
                    }
                    "REVOKED" -> {
                        setupViewPager(getNormalStateItems())
                        binding.patronButton.isEnabled = false
                    }
                }
            }
        } else {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.baseImageTextLayout.visibility = View.GONE
            binding.managePatronStateLayout.visibility = View.GONE
            binding.requestStateLayout.visibility = View.GONE
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
        subscribeToInvite()
    }

    private fun subscribeToInvite() {
        model.patronInviteStatus.observe(viewLifecycleOwner, { inviteStatus ->
            if (inviteStatus == "Success") {
                user.patronInvitationStatus = "REQUESTED"
                handleUIStates()
            } else {
                binding.root.snackbar("Patron Invitation wasn't requested")
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

                }
                "REVOKED" -> {

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
                //intent.putExtra("page", "categories")
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