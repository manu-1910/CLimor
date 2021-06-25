package com.limor.app.scenes.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.databinding.ActivityFollowersAndFollowingBinding
import com.limor.app.scenes.main.fragments.profile.FollowViewModelNew
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowersAndFollowingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowersAndFollowingBinding
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val  viewModel: SettingsViewModel by viewModels { viewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_followers_and_following) as ActivityFollowersAndFollowingBinding

        intent?.extras?.get("tab")?.let{
            when(it){
                "Followers" -> binding.toggleGender.check(R.id.btnCasts)
                "Following" -> binding.toggleGender.check(R.id.btnPatron)
                else -> binding.toggleGender.check(R.id.btnCasts)
            }
        }

        lifecycleScope.launch(Dispatchers.Main){
           val followers =  viewModel.getFollowers(0)
           followers?.let{
                addToggleClickListener()
           }
        }

    }

    private fun addToggleClickListener() {
        binding.btnFollowers.text = "Followers"
        binding.btnFollowing.text = "Following"
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