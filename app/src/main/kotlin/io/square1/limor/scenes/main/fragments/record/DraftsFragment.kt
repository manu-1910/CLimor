package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_cross.*
import org.jetbrains.anko.sdk23.listeners.onClick


class DraftsFragment : BaseFragment() {


    companion object {
        val TAG: String = DraftsFragment::class.java.simpleName
        fun newInstance() = DraftsFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drafts, container, false)
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

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_drafts)

        //Toolbar Left
        btnClose.onClick {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_edit)
        btnToolbarRight.onClick {
            try {
                //Setup animation transition
                ViewCompat.setTranslationZ(view!!, 1f)

                findNavController().navigate(R.id.action_record_drafts_to_record_edit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




}

