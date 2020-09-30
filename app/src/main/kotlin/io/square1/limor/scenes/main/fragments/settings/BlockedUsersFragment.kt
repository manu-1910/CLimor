package io.square1.limor.scenes.main.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import javax.inject.Inject

class BlockedUsersFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var rootView: View? = null
    var app: App? = null



    companion object {
        val TAG: String = BlockedUsersFragment::class.java.simpleName
        fun newInstance() = BlockedUsersFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_users_blocked, container, false)
        }
        app = context?.applicationContext as App
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)
    }

}