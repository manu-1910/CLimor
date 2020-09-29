package io.square1.limor.scenes.main.fragments.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.uimodels.UIUser
import kotlinx.android.synthetic.main.activity_report_user.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.sdk23.listeners.onClick

enum class TypeReport {
    COMMENT, CAST, USER
}

class ReportActivity : BaseActivity() {

    var typeReport : TypeReport? = TypeReport.USER

    var uiUser : UIUser? = null

    companion object {
        val TAG: String = ReportActivity::class.java.simpleName
        fun newInstance() = ReportActivity()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_user)

        val bundle = intent?.extras
        uiUser = bundle?.get("user") as UIUser?
        typeReport = bundle?.get("type") as TypeReport?
        setupToolbar()
        setupScreen()
        initListeners()
    }

    private fun setupScreen() {
        etReason.hint = when(typeReport) {
            TypeReport.COMMENT -> getString(R.string.hint_report_comment)
            TypeReport.CAST -> getString(R.string.hint_report_cast)
            TypeReport.USER -> getString(R.string.hint_report_user)
            null -> getString(R.string.hint_report_user)
        }
    }

    private fun initListeners() {
        btnToolbarLeft.onClick { finish() }
        btnToolbarRight.onClick {
            val intentResult = Intent()
            intentResult.putExtra("reason", etReason.text.toString())
            setResult(Activity.RESULT_OK, intentResult)
            finish()
        }
    }

    private fun setupToolbar() {
        btnToolbarLeft.text = getString(R.string.cancel)
        btnToolbarRight.text = getString(R.string.report)
        tvToolbarTitle.text = when(typeReport) {
            TypeReport.COMMENT -> getString(R.string.title_report_comment)
            TypeReport.CAST -> getString(R.string.title_report_cast)
            TypeReport.USER -> getString(R.string.title_report_user)
            null -> getString(R.string.title_report_user)
        }
    }


}