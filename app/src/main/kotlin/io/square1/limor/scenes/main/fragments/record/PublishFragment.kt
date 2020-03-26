package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_cross.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast


class PublishFragment : BaseFragment() {


    companion object {
        val TAG: String = PublishFragment::class.java.simpleName
        fun newInstance() = PublishFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_publish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()
        listeners()
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
        tvToolbarTitle?.text = getString(R.string.title_publish)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.visibility = View.GONE

    }


    private fun listeners(){

        placeHolder.onClick {
            toast("Choose a photo here")
        }

        btnSaveAsDraft.onClick {
            toast("Save as Daft here")
        }

        btnPublish.onClick {
            toast ("Publish here")
        }
    }



}

