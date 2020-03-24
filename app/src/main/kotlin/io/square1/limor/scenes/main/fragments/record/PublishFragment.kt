package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast


class PublishFragment : BaseFragment() {


    private lateinit var btnToolbarLeft: Button
    private lateinit var btnToolbarRight: Button


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

        tvToolbarTitle?.text = getString(R.string.title_record)

        //Toolbar Left
        //btnToolbarLeft.background = getDrawable(R.drawable.upload)
        //btnToolbarLeft.setBackgroundResource(R.drawable.upload)
        //btnToolbarLeft.height = 16
        btnToolbarLeft.text = "Cancel"
        btnToolbarLeft.onClick {
            //this.finish()
            toast("Clicked on Cancel")
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_drafts)
        btnToolbarRight.onClick {
            toast("Clicked on Drafts")
        }
    }




}

