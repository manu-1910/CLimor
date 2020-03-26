package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert


class EditFragment : BaseFragment() {


    companion object {
        val TAG: String = EditFragment::class.java.simpleName
        fun newInstance() = EditFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()

    }



    private fun bindViewModel() {
        /* activity?.let { fragmentActivity ->
             mainViewModel = ViewModelProviders
                 .of(fragmentActivity, viewModelFactory)
                 .get(MainViewModel::class.java)
         }*/
    }




    private fun configureToolbar() {

        //Toolbar Title
        tvToolbarTitle?.text = getString(R.string.title_edit)

        //Toolbar left
        btnClose.onClick {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //Toolbar Right
        btnInfo.onClick {
            alert("info dialog") {
                okButton { }
            }.show()
        }

    }




}

