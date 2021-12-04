package com.limor.app.scenes.patron.manage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.limor.app.databinding.ActivityManagePatronBinding
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class ManagePatronActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var binding: ActivityManagePatronBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagePatronBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
}