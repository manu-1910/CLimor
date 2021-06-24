package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.profile.adapters.UserFollowersAdapter
import com.limor.app.scenes.main.viewmodels.CreateFriendViewModel
import com.limor.app.scenes.main.viewmodels.DeleteFriendViewModel
import com.limor.app.scenes.main.viewmodels.GetUserFollowersViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_followers_followings.*
import kotlinx.android.synthetic.main.activity_followers_followings.view.*
import kotlinx.android.synthetic.main.fragment_user_followers.*
import kotlinx.android.synthetic.main.toolbar_discover.view.*
import javax.inject.Inject


class UserFollowersFragmentNew() : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_user_followers, container, false)

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }





}
