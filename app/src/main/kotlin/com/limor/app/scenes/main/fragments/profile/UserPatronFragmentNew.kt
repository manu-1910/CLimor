package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.limor.app.R
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.patron.FragmentShortItemSlider
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.UserUIModel
import kotlinx.android.synthetic.main.fragment_waveform.view.*
import java.time.Duration

class UserPatronFragmentNew(val user: UserUIModel): Fragment() {

    lateinit var binding:FragmnetUserPatronNewBinding
    var requested = false
    companion object {
        fun newInstance(user: UserUIModel) = UserPatronFragmentNew(user)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmnetUserPatronNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupViewPager(items: ArrayList<FragmentShortItemSlider>) {
        binding.pager.adapter = ShortPagerAdapter(items,childFragmentManager,lifecycle)
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getNormalStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_welcome)
        val item2 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_welcome)
        val item3 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_welcome)
        return arrayListOf(item1,item2,item3)
    }

    private fun getApprovedStateItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_invite_accepted)
        return arrayListOf(item1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (user.id) {
            PrefsHandler.getCurrentUserId(requireContext()) -> {
                //Current user
                handleUIStates(true)
            }
            else -> {
                //Other user
                handleUIStates(false)
            }
        }
        setOnClicks()


    }

    private fun handleUIStates(currentUser: Boolean){
        binding.emptyStateTv.text =  if (currentUser) {getString(R.string.limor_patron_empty_state)} else getString(R.string.patron_empty_state_other)
        binding.patronButton.isEnabled = true
        if(currentUser){
            if(user.isPatron == true){
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
                        //Show empty state here
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.baseImageTextLayout.visibility = View.GONE
                        binding.managePatronStateLayout.visibility = View.GONE
                        binding.requestStateLayout.visibility = View.GONE
                    }
                    "NOT_REQUESTED" -> {
                        //Show Request Invite state
                        setupViewPager(getNormalStateItems())
                        binding.patronButton.text = getString(R.string.request_invite)
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.baseImageTextLayout.visibility = View.VISIBLE
                        binding.managePatronStateLayout.visibility = View.GONE
                        binding.requestStateLayout.visibility = View.VISIBLE
                    }
                    "REQUESTED" -> {
                        setupViewPager(getNormalStateItems())
                        binding.patronButton.isEnabled = false
                        binding.patronButton.text = getString(R.string.requested)
                        binding.prProgress.progress = 0
                    }
                    "APPROVED" -> {
                        //Approved but note yet setup
                        setupViewPager(getApprovedStateItems())
                        binding.patronButton.text = getString(R.string.limorPatronSetup)
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
        }else{
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.baseImageTextLayout.visibility = View.GONE
            binding.managePatronStateLayout.visibility = View.GONE
            binding.requestStateLayout.visibility = View.GONE
        }

    }

    private fun setupAudioPlayer(url:String?, durationSeconds:Double?) {
        if (url.isNullOrEmpty()) {
            binding.audioPlayer.visibility = View.GONE
        } else {
            val durationMillis = ((durationSeconds ?: 0.0) * 1000.0).toLong()
            binding.audioPlayer.initialize(
                AudioCommentUIModel(
                    url = url,
                    duration = Duration.ofMillis(durationMillis)
                )
            )
            binding.audioPlayer.visibility = View.VISIBLE
        }
    }

    private fun setOnClicks() {

        binding.patronButton.setOnClickListener {
            when (user.patronInvitationStatus) {
                "NOT_REQUESTED" -> {
                    //Should request patron invitation
                    binding.patronButton.isEnabled = false
                    binding.patronButton.text = getString(R.string.requested)
                    binding.prProgress.progress = 0

                    user.patronInvitationStatus = "APPROVED"
                    handleUIStates(true)
                }
                "APPROVED" -> {
                    //Should setup Limor patron


                }
                "REJECTED" -> {

                }
                "REVOKED" -> {

                }
            }
        }
    }


}