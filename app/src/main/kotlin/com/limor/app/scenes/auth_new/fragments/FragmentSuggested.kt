package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.SuggestedUser
import com.limor.app.scenes.auth_new.navigation.AuthNavigator
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.auth_new.view.SuggestedPeopleAdapter
import kotlinx.android.synthetic.main.fragment_new_auth_suggested_people.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class FragmentSuggested : FragmentWithLoading() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_suggested_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        saveNavigationBreakPoint()
    }

    override fun load() = model.downloadSuggested()

    override val errorLiveData: LiveData<String>
        get() = model.suggestedLiveDataError

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.suggestedUsersLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                return@Observer
            switchCommonVisibility()
            buildList(it)
        })

        model.suggestedSelectedLiveData.observe(viewLifecycleOwner, {
            btnNext.isEnabled = it
        })

        model.updateOnboardingStatusLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d("Update onboarding status liveData suggested $it")
                lifecycleScope.launch {
                    delay(1000)
                    Timber.d("Navigate to Onboarding")
                    saveNavigationBreakPoint(NavigationBreakpoints.ONBOARDING_COMPLETION.destination)
                    navigateToHomeFeed()
                }
            }
        })

        model.suggestedForwardNavigationLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                model.updateUserOnboardingStatus(NavigationBreakpoints.ONBOARDING_COMPLETION.destination)
            }
        })
    }

    private fun buildList(users: List<SuggestedUser>) {
        rvSuggestedUsers.adapter = object : SuggestedPeopleAdapter(users) {
            override fun onSuggestedFollowClicked(user: SuggestedUser) {
                model.followSuggestedUser(user)
            }
        }
    }

    private fun setOnClickListeners() {
        btnNext.setOnClickListener {
            model.sendSuggestedPeopleSelectionResult()
        }

        topAppBar.setNavigationOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }
    }

    private fun saveNavigationBreakPoint() {
        model.saveNavigationBreakPoint(
            requireContext(),
            NavigationBreakpoints.SHOW_PROFILES.destination
        )
    }

    private fun saveNavigationBreakPoint(destination: String?) {
        model.saveNavigationBreakPoint(requireContext(), destination)
    }

    private fun navigateToHomeFeed() {
        model.updateOnboardingStatusLiveData.removeObservers(viewLifecycleOwner)
        PrefsHandler.saveJustLoggedIn(requireContext(), true)
        view?.findNavController()?.navigate(R.id.go_to_main_activity)
        Timber.d("trying to finish activity")
        saveNavigationBreakPoint(NavigationBreakpoints.HOME_FEED.destination)
        model.updateUserOnboardingStatus(NavigationBreakpoints.HOME_FEED.destination)
        requireActivity().finish()
    }

}