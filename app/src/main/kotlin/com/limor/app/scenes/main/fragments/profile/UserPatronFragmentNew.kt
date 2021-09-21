package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.limor.app.R
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import com.limor.app.scenes.patron.FragmentShortItemSlider
import kotlinx.android.synthetic.main.fragment_waveform.view.*

class UserPatronFragmentNew(): Fragment() {

    lateinit var binding:FragmnetUserPatronNewBinding
    var requested = false
    companion object {
        fun newInstance(newUserId: Int) = UserPatronFragmentNew()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmnetUserPatronNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupViewPager() {
        val items : ArrayList<FragmentShortItemSlider> = getAdapterItems()
        binding.pager.adapter = ShortPagerAdapter(items,childFragmentManager,lifecycle)
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getAdapterItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_welcome)
        val item2 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_welcome)
        val item3 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,R.drawable.ic_patron_welcome)
        return arrayListOf(item1,item2,item3)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setOnClicks()

    }

    private fun setOnClicks() {

        binding.patronButton.setOnClickListener {
            if(!requested){
                binding.patronButton.isEnabled = false
                binding.patronButton.text = getString(R.string.requested)
                binding.prProgress.progress = 0
            }
        }
    }


}