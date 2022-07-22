package com.limor.app.scenes.notifications

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.databinding.FragmentNotificationBinding
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.uimodels.NotiUIMode
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.android.synthetic.main.toolbar_discover.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.anko.image
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
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

    fun getData() {
        lifecycleScope.launch {
            notificationViewModel.getNotifications().collectLatest {
                notificationAdapter.submitData(it)
            }
        }
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        setupClickListeners()
        notificationAdapter = NotificationAdapter(requireContext())
        binding.notificationList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = notificationAdapter
        }

        getData()

        notificationAdapter.openCastCallback {
            it?.let { castId ->
                (activity as MainActivityNew).openExtendedPlayer(castId)
            }
        }
        notificationAdapter.addUserTypeCallback{
            userId, username, tab ->
            UserProfileActivity.show(requireContext(),username!!,userId!!,tab)
        }
        notificationAdapter.addNotificationReadListener { nId, read ->
            nId?.let {
                notificationViewModel.updateReadStatus(it, read)
            }
        }
        notificationAdapter.addNoInternetAlertCallback {
            showInternetErrorDialog()
        }
        notificationAdapter.addCommentsSectionCallback { commentId, childCommentId, castId ->
            if(activity is MainActivityNew){
                (activity as MainActivityNew).openCommentsSections(commentId, childCommentId, castId)
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

    private fun showInternetErrorDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_error_publish_cast, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog: AlertDialog = dialogBuilder.create()

        dialogView.imageView.image = resources.getDrawable(R.drawable.ic_alert)
        dialogView.textTitle.text = resources.getString(R.string.no_connection_title)
        dialogView.textDescription.text = resources.getString(R.string.no_connection_description)
        dialogView.okButton.text = resources.getString(R.string.ok)
        dialogView.cancelButton.onClick {
            dialog.dismiss()
        }
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