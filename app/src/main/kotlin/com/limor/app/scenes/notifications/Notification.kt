package com.limor.app.scenes.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.databinding.FragmentNotificationBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.uimodels.NotiUIMode
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class Notification : Fragment() {
    private val notificationsList = ArrayList<NotiUIMode>()
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var notificationAdapter: NotificationAdapter
    lateinit var binding: FragmentNotificationBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val notificationViewModel: NotificationViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        setupClickListeners()
        notificationAdapter = NotificationAdapter(requireContext(), notificationsList)
        binding.notificationList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = notificationAdapter
        }

        notificationViewModel.noti.observe(viewLifecycleOwner, Observer {
            it?.let {
                notificationAdapter.addItems(it)
            }
        })

        notificationAdapter.openCastCallback{
            it?.let{ castId ->
                (activity as MainActivityNew).openExtendedPlayer(castId)
            }
        }
        notificationAdapter.addUserTypeCallback{
            userId, username ->
            val userProfileIntent = Intent(requireContext(), UserProfileActivity::class.java)
            userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, username)
            userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, userId)
            startActivity(userProfileIntent)
        }
        notificationAdapter.addNotificationReadListener { nId, read ->
            nId?.let{
                notificationViewModel.updateReadStatus(it,read)
            }
        }
        getNotifications()
    }

    private fun getNotifications() {
        notificationViewModel.loadNotifications()
    }

    private fun setupClickListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}