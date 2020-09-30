package io.square1.limor.scenes.main.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.viewmodels.SignViewModel
import io.square1.limor.scenes.main.viewmodels.ChangePasswordViewModel
import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class EditProfileFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var changePasswordViewModel: ChangePasswordViewModel
    private lateinit var signInViewModel: SignViewModel

    private val changePasswordTrigger = PublishSubject.create<Unit>()
    private val signTrigger = PublishSubject.create<Unit>()

    private var rootView: View? = null
    var app: App? = null



    companion object {
        val TAG: String = EditProfileFragment::class.java.simpleName
        fun newInstance() = EditProfileFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        }
        app = context?.applicationContext as App
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        configureToolbar()
        //apiCallChangePassword()

    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.settings_edit_profile)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btnUpdate)
        btnToolbarRight.visibility = View.VISIBLE
        btnToolbarRight.onClick {
           toast("update clicked")
        }
    }


    private fun bindViewModel() {
        activity?.let {
            changePasswordViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(ChangePasswordViewModel::class.java)

            signInViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(SignViewModel::class.java)
        }
    }





    private fun callToApiChangePassword(){
        changePasswordTrigger.onNext(Unit)
    }


    private fun apiCallChangePassword() {
        val output = changePasswordViewModel.transform(
            ChangePasswordViewModel.Input(
                changePasswordTrigger
            )
        )

        output.response.observe(this, Observer {
            pbChangePassword?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) {
                toast("Password changed successfully")
                signTrigger.onNext(Unit)
            }

        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbChangePassword?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
//                if(it.code == 10){  //Session expired
//                    alert(message.toString()) {
//                        okButton {
//                            val intent = Intent(context, SignActivity::class.java)
//                            //intent.putExtra(getString(R.string.otherActivityKey), true)
//                            startActivityForResult(
//                                intent,
//                                resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH)
//                            )
//                        }
//                    }.show()
//                }else{
//                    alert(message.toString()) {
//                        okButton { }
//                    }.show()
//                }
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }



}

