package com.limor.app.scenes.main_new.fragments.comments

import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main_new.fragments.mentions.UserMentionPopup
import com.limor.app.scenes.main_new.view_model.UserMentionViewModel
import javax.inject.Inject

abstract class UserMentionFragment : BaseFragment(), UserMentionPopup.UserMentionData {

    private lateinit var mentionPopup: UserMentionPopup

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val userModel: UserMentionViewModel by viewModels { viewModelFactory }

    protected fun setUpPopup(editText: EditText, editArea: View) {
        mentionPopup = UserMentionPopup(editText, this)
        userModel.userMentionData.observe(viewLifecycleOwner) { users ->
            mentionPopup.setUsers(users)
        }

        mentionPopup.inputHeight = resources.getDimension(R.dimen.commentFooterHeight).toInt()
    }

    override fun search(text: String) {
        userModel.search(text)
    }
}