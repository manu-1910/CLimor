package com.limor.app.scenes.patron

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.databinding.FragmentShortItemSliderBinding
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.text.Spanned


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentShortItemSlider.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentShortItemSlider : Fragment() {
    // TODO: Rename and change types of parameters
    private var text: Int? = null
    private var imageRes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            text = it.getInt(ARG_PARAM1)
            imageRes = it.getInt(ARG_PARAM2)
        }
    }

    private fun fixSpanColor(text: CharSequence): CharSequence {
        return if (text is Spanned) {
            val s = SpannableString(text)
            val spans = s.getSpans(
                0, s.length,
                ForegroundColorSpan::class.java
            )
            for (oldSpan in spans) {
                val newSpan = ForegroundColorSpan(oldSpan.foregroundColor or -0x1000000)
                s.setSpan(
                    newSpan,
                    s.getSpanStart(oldSpan),
                    s.getSpanEnd(oldSpan),
                    s.getSpanFlags(oldSpan)
                )
                s.removeSpan(oldSpan)
            }
            s
        } else {
            text
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentShortItemSliderBinding.inflate(inflater, container, false)
        imageRes?.let {
            binding.patronStatusIv.setImageResource(it)
        }
        text?.let {
            binding.patronStatusTv.text = fixSpanColor(getText(it))
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentShortItemSlider.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            FragmentShortItemSlider().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                }
            }
    }
}