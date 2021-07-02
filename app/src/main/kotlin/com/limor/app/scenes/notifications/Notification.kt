package com.limor.app.scenes.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.databinding.FragmentNotificationBinding
import kotlinx.android.synthetic.main.fragment_notification.*

class Notification : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<NotificationAdapter.ViewHolder>? = null
    lateinit var binding: FragmentNotificationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        setupClickListeners()
        binding.notificationList.apply {


            layoutManager = LinearLayoutManager(activity)

            adapter = NotificationAdapter()
        }


    }

    private fun setupClickListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}