package com.limor.app.scenes.patron

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.R
import com.limor.app.databinding.FragmentPatronOnboardingBinding
import com.limor.app.databinding.FragmnetUserPatronNewBinding
import com.limor.app.scenes.main.fragments.profile.ShortPagerAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPatronOnboarding.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPatronOnboarding : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding:FragmentPatronOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentPatronOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentPatronOnboarding.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentPatronOnboarding().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupViewPager() {
        val items : ArrayList<FragmentShortItemSlider> = getAdapterItems()
        binding.pager.adapter = ShortPagerAdapter(items,childFragmentManager,lifecycle)
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getAdapterItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(R.string.patron_carousel_slide_1_title,R.drawable.patron_carousel_slide_1_image,R.string.patron_carousel_slide_1_sub_title)
        val item2 = FragmentShortItemSlider.newInstance(R.string.patron_carousel_slide_2_title,R.drawable.patron_carousel_slide_2_image,R.string.patron_carousel_slide_2_sub_title)
        val item3 = FragmentShortItemSlider.newInstance(R.string.patron_carousel_slide_3_title,R.drawable.patron_carousel_slide_3_image,R.string.patron_carousel_slide_3_sub_title)
        return arrayListOf(item1,item2,item3)
    }
}