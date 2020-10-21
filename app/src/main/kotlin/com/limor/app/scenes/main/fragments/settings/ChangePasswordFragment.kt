package com.limor.app.scenes.main.fragments.settings

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
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.authentication.viewmodels.SignViewModel
import com.limor.app.scenes.main.viewmodels.ChangePasswordViewModel
import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class ChangePasswordFragment : BaseFragment() {

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
        val TAG: String = ChangePasswordFragment::class.java.simpleName
        fun newInstance() = ChangePasswordFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_change_password, container, false)
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
        apiCallChangePassword()
        apiCallSignInWithMail()
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.settings_change_password)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btnUpdate)
        btnToolbarRight.visibility = View.VISIBLE
        btnToolbarRight.onClick {
            changePasswordViewModel.newPasswordViewModel = etNewPassword.text.toString()
            changePasswordViewModel.currentPasswordViewModel = etCurrentPassword.text.toString()

            if(checkEmptyFields()){
                if(checkPasswordsMatch()){
                    if(checkPasswordLength()){
                        callToApiChangePassword()
                    }
                }
            }
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


    private fun checkEmptyFields(): Boolean {
        return if (etCurrentPassword?.text.isNullOrEmpty() || etNewPassword?.text.isNullOrEmpty() || etConfirmPassword?.text.isNullOrEmpty()) {
            alert(getString(R.string.all_fields_mandatory)) {
                okButton {}
            }.show()
            false
        }else{
            true
        }
    }

    private fun checkPasswordLength():Boolean{
        return if(etNewPassword.text!!.isNotBlank() && (etNewPassword.text!!.length >= resources.getInteger(R.integer.PASSWORD_MIN_LENGTH))) {
            true
        }else{
            alert(getString(R.string.error_not_valid_password)) {
                okButton {}
            }.show()
            false
        }
    }

    private fun checkPasswordsMatch(): Boolean{
        return if(etNewPassword.text?.toString() == etConfirmPassword.text?.toString()){
            true
        }else{
            alert(getString(R.string.passwords_not_match)) {
                okButton {}
            }.show()
            false
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


    private fun apiCallSignInWithMail() {


        val output = signInViewModel.transform(
            SignViewModel.Input(
            etCurrentPassword.textChanges().map { sessionManager.getStoredUser()?.email },
            etNewPassword.textChanges().map { it.toString() },
            signTrigger
        ))

        output.response.observe(this, Observer {
            if (it) {
                toast(getString(R.string.change_password_ok))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
            if (it)
                pbSignIn?.visibility = View.VISIBLE
            else {
                pbSignIn?.visibility = View.GONE
                view?.hideKeyboard()
            }
        })

        output.errorMessage.observe(this, Observer {
            if (app!!.merlinsBeard!!.isConnected) {
                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }
                alert(message.toString()) {
                    okButton { }
                }.show()
            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }

}

