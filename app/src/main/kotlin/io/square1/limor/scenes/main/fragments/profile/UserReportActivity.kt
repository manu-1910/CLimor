package io.square1.limor.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.activity_report_user.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.sdk23.listeners.onClick

class UserReportActivity : BaseActivity() {

    var uiUser : UIUser? = null

    companion object {
        val TAG: String = UserReportActivity::class.java.simpleName
        fun newInstance() = UserReportActivity()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_user)

        val bundle = intent?.extras
        uiUser = bundle?.get("user") as UIUser?

        setupToolbar()
        initListeners()
    }

    private fun initListeners() {
        btnToolbarLeft.onClick { finish() }
        btnToolbarRight.onClick {
            val intentResult = Intent()
            intentResult.putExtra("reason", etReason.text.toString())
            setResult(0, intentResult)
            finish()
        }
    }

    private fun setupToolbar() {
        btnToolbarLeft.text = getString(R.string.cancel)
        btnToolbarRight.text = getString(R.string.report)
        tvToolbarTitle.text = getString(R.string.report_user)
    }


}