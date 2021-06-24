package com.limor.app.scenes.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.databinding.ActivityFollowersAndFollowingBinding
import com.limor.app.scenes.main.fragments.profile.FollowViewModelNew
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowersAndFollowingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowersAndFollowingBinding
    private val viewModel: FollowViewModelNew by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_followers_and_following) as ActivityFollowersAndFollowingBinding


        lifecycleScope.launch(Dispatchers.Main){
           val followers =  viewModel.getFollowers()
           followers?.let{
                addToggleClickListener()
           }
        }

    }

    private fun addToggleClickListener() {
        binding.btnCasts.text = "Followers"
        binding.btnPatron.text = "Following"
        binding.toggleGender.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
               /* val gender = when (checkedId) {
                    R.id.btnGender1 -> list[0]
                    R.id.btnGender2 -> list[1]
                    else -> list[0]
                }*/
               // viewModel.selectGender(gender.id ?: 0)
            }
        }
    }
}