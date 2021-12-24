package com.limor.app

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.hendraanggrian.appcompat.widget.Hashtag
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView
import com.limor.app.databinding.ActivityEditCastBinding
import com.limor.app.scenes.main.fragments.record.adapters.HashtagAdapter
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.main.viewmodels.TagsViewModel
import com.limor.app.scenes.utils.CommonsKt.Companion.toEditable
import com.limor.app.scenes.utils.waveform.KeyboardUtils
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_edit_cast.*
import kotlinx.android.synthetic.main.activity_edit_cast.etHashtags
import kotlinx.android.synthetic.main.fragment_publish.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon_light.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Matcher
import java.util.regex.Pattern
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
    private val tagsViewModel: TagsViewModel by viewModels { viewModelFactory }

    private var twHashtags: TextWatcher? = null
    private var twCaption: TextWatcher? = null
    private var twTitle: TextWatcher? = null
    private var rvTags: RecyclerView? = null
    private var etHashTags: SocialAutoCompleteTextView? = null

    private var listTags = ArrayList<TagUIModel>()
    private var listTagsString: HashtagArrayAdapter<Hashtag>? = null

    private var isTagsSelected: Boolean = false
    private var isShowingTagsRecycler = false

    private var etDraftTitle: TextInputEditText? = null
    private var etDraftCaption: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_edit_cast)
        initialise()

        setListeners()
        apiCallHashTags()
    }

    override fun onResume() {
        super.onResume()
        loadExistingData()
        multiCompleteText()
        setupRecyclerTags()
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
        rvTags = findViewById(R.id.rvHashtags)
        etHashTags = findViewById(R.id.etHashtags)

        listTagsString = HashtagArrayAdapter(this)
        tvToolbarTitle.setText(R.string.edit_cast)
    }

    private fun callToTagsApiAndShowRecyclerView(tag: String) {
        tagsViewModel.tagToSearch = tag
        tagsViewModel.searchHashTags(tag)
    }

    private fun apiCallHashTags(){
        tagsViewModel.searchResult.observe(this, Observer{
            it?.let{
                if(it.isNotEmpty()){
                    listTagsString?.clear()
                    listTags.clear()
                    listTags.addAll(it)
                    for (item in listTags) {
                        listTagsString?.add(Hashtag(item.tag))
                    }
                    rvTags?.adapter?.notifyDataSetChanged()
                }
            }
        })
    }

    private fun multiCompleteText() {
        etHashTags?.isMentionEnabled = false
        etHashTags?.hashtagColor =
            ContextCompat.getColor(this, R.color.textPrimary)
        etHashTags?.hashtagAdapter = null
        etHashTags?.setHashtagTextChangedListener { _, text ->
            println("setHashtagTextChangedListener -> $text")
            callToTagsApiAndShowRecyclerView(text.toString())
        }
    }

    private fun setupRecyclerTags() {
        rvTags?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTags?.adapter = listTagsString?.let {
            HashtagAdapter(it, object : HashtagAdapter.OnItemClickListener {
                override fun onItemClick(item: Hashtag) {
                    val actualString = etHashtags.text
                    val finalString: String =
                        actualString.substring(
                            0, actualString.lastIndexOf(
                                getCurrentWord(
                                    etHashtags
                                )!!
                            )
                        ) +
                                "#$item" +
                                actualString.substring(
                                    actualString.lastIndexOf(
                                        getCurrentWord(
                                            etHashtags
                                        )!!
                                    ) + getCurrentWord(etHashtags)!!.length, actualString.length
                                ) + " "

                    etHashTags?.setText(finalString)
                    etHashTags?.setSelection(etHashTags?.text?.length ?: 0)
                    //This places cursor to end of EditText.
                    rvTags?.adapter?.notifyDataSetChanged()
                }
            })
        }
    }

    fun getCurrentWord(editText: EditText): String? {
        val textSpan: Spannable = editText.text
        val regex = Regex("#\\w+")
        val pattern: Pattern = Pattern.compile(regex.pattern)
        val matcher: Matcher = pattern.matcher(textSpan)
        var currentWord = ""
        while (matcher.find()) {
            currentWord = matcher.group(matcher.groupCount())
        }
        return currentWord // This is current word
    }

    private fun setListeners(){
        twHashtags = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                etHashTags?.hashtags?.let{
                    isTagsSelected = it.size > 0
                    publishViewModel.tags.clear()
                    publishViewModel.tags.addAll(it.map { "#$it" })
                    updateSaveButton()
                }

            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                if (s.isEmpty()) {
                    rvTags?.visibility = View.GONE
                    //lytWithoutTagsRecycler?.visibility = View.VISIBLE
                    isShowingTagsRecycler = false
                } else {
                    val cleanString = s.toString().replace(System.lineSeparator(), " ")
                    val lastSpaceIndex = cleanString.lastIndexOf(" ")
                    val lastWord = if (lastSpaceIndex >= 0) {
                        cleanString.substring(lastSpaceIndex).trim()
                    } else {
                        cleanString.trim()
                    }
                    val pattern = "#\\w+".toRegex()

                    if (pattern.matches(lastWord)) {
                        rvTags?.visibility = View.VISIBLE
                        lytWithoutTagsRecycler?.visibility = View.GONE
                        isShowingTagsRecycler = true

                    } else {
                        rvTags?.visibility = View.GONE
                        lytWithoutTagsRecycler?.visibility = View.VISIBLE
                        isShowingTagsRecycler = false
                    }
                }
            }
        }
        etHashtags.addTextChangedListener(twHashtags)

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


        KeyboardUtils.addKeyboardToggleListener(this) { isVisible ->
            if (isShowingTagsRecycler) {
                if (isVisible) {
                    rvTags?.visibility = View.VISIBLE
                    lytWithoutTagsRecycler?.visibility = View.GONE
                } else {
                    rvTags?.visibility = View.GONE
                    lytWithoutTagsRecycler?.visibility = View.VISIBLE
                }
            }
        }
        btnClose.setOnClickListener {
            finish()
        }
        btnSave.setOnClickListener {
            lifecycleScope.launch {
                val title = etDraftTitle?.text?.trim().toString() ?: ""
                val id = intent?.getIntExtra(EditCastActivity.ID, -1) ?: -1
                val caption = etDraftCaption?.text.toString() ?: ""
                val tags = etHashtags?.text.toString() ?: ""
                val fullCaption = "$caption${if (caption.isEmpty()) "" else "\n\n"}$tags"

                val matureContent = false
                val response = withContext(Dispatchers.IO) {
                    publishViewModel.updatePodcast(id, title, fullCaption, matureContent)
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
        btnSave?.isEnabled = isTitleValid && isCaptionValid && isTagsSelected
    }

    override fun onDestroy(){
        super.onDestroy()
    }
}