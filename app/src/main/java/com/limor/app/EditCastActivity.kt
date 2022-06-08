package com.limor.app

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.limor.app.databinding.ActivityEditCastBinding
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.utils.CommonsKt.Companion.toEditable
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_edit_cast.*
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon_light.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EditCastActivity : AppCompatActivity() {

    companion object{
        const val TITLE: String = "TITLE"
        const val CAPTION_TAGS: String = "CAPTION_TAGS"
        const val CAPTION: String = "CAPTION"
        const val TAGS: String = "TAGS"
        const val ID: String = "CAST_ID"
        const val MATURE_CONTENT: String = "MATURE_CONTENT"
    }

    lateinit var binding: ActivityEditCastBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val publishViewModel: PublishViewModel by viewModels { viewModelFactory }

    private var twCaption: TextWatcher? = null
    private var twTitle: TextWatcher? = null

    private var etDraftTitle: TextInputEditText? = null
    private var etDraftCaption: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_edit_cast)
        initialise()

        setListeners()
    }

    override fun onResume() {
        super.onResume()
        loadExistingData()
    }

    private fun loadExistingData(){
        val title = intent.getStringExtra(TITLE) ?: ""
        val captionTags = intent.getStringExtra(CAPTION_TAGS) ?: ""
        if (title.trim().isNotEmpty()) {
            if (!title.trim().equals(getString(R.string.autosave))) {
                etDraftTitle?.text = title.toEditable()
            }
        }
        if (captionTags.trim().isNotEmpty()) {
            val captionAndTags = captionTags.split(resources.getString(R.string.new_line_seperator))
            var caption = ""
            var tags = ""
            if(captionAndTags.size == 2){
                caption = captionAndTags[0]
                tags = captionAndTags[1]
                etDraftCaption?.text = caption.toEditable()
                etHashtags?.text = tags.toEditable()
            } else{
                etDraftCaption?.text = captionTags.toEditable()
            }
        }
    }

    private fun initialise(){
        etDraftTitle = findViewById(R.id.etTitle)
        etDraftCaption = findViewById(R.id.etCaption)
        tvToolbarTitle.setText(R.string.edit_cast)
    }

    private fun setListeners(){
        twTitle = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                updateSaveButton()
            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {}
        }
        etDraftTitle?.addTextChangedListener(twTitle)

        twCaption = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                updateSaveButton()
            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {}
        }
        etDraftCaption?.addTextChangedListener(twCaption)

        btnClose.setOnClickListener {
            finish()
        }
        btnSave.setOnClickListener {
            lifecycleScope.launch {
                val title = etDraftTitle?.text?.trim().toString() ?: ""
                val id = intent?.getIntExtra(EditCastActivity.ID, -1) ?: -1
                val caption = etDraftCaption?.text.toString()

                val matureContent = false
                val response = withContext(Dispatchers.IO) {
                    publishViewModel.updatePodcast(id, title, caption, matureContent)
                }
                response.let {
                    setResult(Activity.RESULT_OK)
                    finish()
                }?:run{
                    Toast.makeText(btnSave.context,"Error while updating the cast",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSaveButton(){
        val isTitleValid = etDraftTitle?.text?.trim()?.isNotEmpty() ?: false
        val isCaptionValid = etDraftCaption?.text?.trim()?.isNotEmpty() ?: false
        btnSave?.isEnabled = isTitleValid && isCaptionValid
    }

    override fun onDestroy(){
        super.onDestroy()
    }
}