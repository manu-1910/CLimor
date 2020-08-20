package io.square1.limor.scenes.main.fragments.record

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.fragments.record.adapters.HashtagsAdapter
import io.square1.limor.scenes.main.viewmodels.TagsViewModel
import io.square1.limor.uimodels.UITags
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import kotlinx.android.synthetic.main.toolbar_with_searchview.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class HashtagsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var tagsViewModel: TagsViewModel

    private var rvHashtags: RecyclerView? = null
    private var rootView: View? = null
    private var adapter: HashtagsAdapter? = null
    private var listTags = ArrayList<UITags>()
    private var listTagsSelected = ArrayList<UITags>()
    private val tagsTrigger = PublishSubject.create<Unit>()

    var app: App? = null



    companion object {
        val TAG: String = HashtagsFragment::class.java.simpleName
        fun newInstance() = HashtagsFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_hashtags, container, false)

            rvHashtags = rootView?.findViewById(R.id.rvHashtags)
        }
        app = context?.applicationContext as App
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()
        //configureAdapter()
        apiCallSearchTags()
        setupRecycler(listTags)
    }

    override fun onResume() {
        super.onResume()

        tagsTrigger.onNext(Unit)
    }


    private fun bindViewModel() {
        activity?.let {
            tagsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(TagsViewModel::class.java)
        }
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_hashtags)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnDone.onClick {
            toast("Done clicked")
//            var test = listTagsSelected
//            println("selected tags are: " +listTagsSelected)

            //viewModelSearchProperties.uiSearchRequest.localityArraySelectedItems.clear()
            //viewModelSearchProperties.uiSearchRequest.localityArray.clear()

            tagsViewModel.localListTagsSelected.clear()
            tagsViewModel.localListTags.clear()

            tagsViewModel.localListTags.addAll(listTags)
            tagsViewModel.localListTagsSelected.addAll(listTagsSelected)

            findNavController().popBackStack()

        }



        //Search View
        search_view.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                //Timber.d("Text changed:$newText")
                if (newText.isNotEmpty() && newText.length > 3){
                    println("entro aquí onquerytextchange")
                    //searchTags(newText)
                    //setupRecycler(listTags)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                //Timber.d("Submit:$query")
                if (query.isNotEmpty() && query.length > 3){
                    searchTags(query)
                }else{
                    toast("You should type at least 3 characters")
                }
                return false
            }

        })
    }


    private fun searchTags(tag: String){
        //Make api call
        tagsViewModel.uiTagsRequest.tag = tag
        tagsTrigger.onNext(Unit)
    }


    private fun apiCallSearchTags() {
        val output = tagsViewModel.transform(
            TagsViewModel.Input(
                tagsTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Tags Response Ok

                listTags.clear()
                listTagsSelected.clear()
                listTagsSelected.addAll(tagsViewModel.localListTagsSelected)

                //Add others localities
                for (tag in it.data.tags) {
                    listTags.add(tag)
                }

                var someTagSelected = false

                //Check if localityArrayItemsSelected is empty or not
                if (listTagsSelected.isNotEmpty()) {
                    for (tag in listTags) {
                        //Set selection
                        for (tagSelected in listTagsSelected) {
                            if (tag.id == tagSelected.id) {
                                tag.isSelected = true
                                someTagSelected = true
                            }
                        }
                    }
                }
                if (someTagSelected){
                    //Set any isSelected false
                    for (tag in listTags){
                        if (tag.id == 0){
                            tag.isSelected = false
                        }
                    }
                }

            }

            //adapter?.notifyDataSetChanged()


            rvHashtags?.adapter?.notifyDataSetChanged()



//                if (it.data.tags.size > 0) {
//                    hidePlaceHolder()
//                    listTags.addAll(it.data.tags)
//                    rvHashtags?.adapter?.notifyDataSetChanged()
//                } else {
//                    showPlaceHolder()
//                }


        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {

                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }

                if(it.code == 10){  //Session expired
                    alert(message.toString()) {
                        okButton {
                            val intent = Intent(context, SignActivity::class.java)
                            //intent.putExtra(getString(R.string.otherActivityKey), true)
                            startActivityForResult(
                                intent,
                                resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH)
                            )
                        }
                    }.show()
                }else{
                    alert(message.toString()) {
                        okButton { }
                    }.show()
                }

            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }


    private fun setupRecycler(tagList: ArrayList<UITags>) {
        rvHashtags?.layoutManager = LinearLayoutManager(context)
        rvHashtags?.adapter = HashtagsAdapter(
            tagList,
            object : HashtagsAdapter.OnItemClickListener {
                override fun onItemClick(item: UITags) {
                    for (tag in listTags) {
                        if (item.text == tag.text) {
                            listTags[listTags.indexOf(item)].isSelected = !listTags[listTags.indexOf(item)].isSelected
                            break
                        }
                    }

                    //If no item selected -> select any
                    var itemSelected = false
                    for (tag in listTags) {
                        if (tag.isSelected){
                            itemSelected = true
                        }
                    }

                    //Add tags to tagsSelectedArray
                    listTagsSelected.clear()
                    for (tag in listTags) {
                        if (tag.isSelected)
                            listTagsSelected.add(tag)
                    }

                    rvHashtags?.adapter?.notifyDataSetChanged()
                }
            })
    }





//    private fun configureAdapter() {
//        val layoutManager = LinearLayoutManager(context)
//        rvHashtags?.layoutManager = layoutManager
//        adapter = HashtagsAdapter(
//            listTags,
//            object : HashtagsAdapter.OnItemClickListener {
//                override fun onItemClick(item: UITags, position: Int) {
////                    val intent = Intent(context, OfficeDetailsActivity::class.java)
//////                    val bundle = Bundle()
//////                    bundle.putSerializable(getString(R.string.centre_key), viewModel.uiCentre)
//////                    bundle.putSerializable(getString(R.string.office_key), item)
//////                    intent.putExtra(getString(R.string.office_bundle_key), bundle)
//////                    startActivity(intent)
//                    toast("item clicked: " + position)
//                }
//            }
//        )
//        rvHashtags?.adapter = adapter
//        rvHashtags?.setHasFixedSize(false)
//    }


}

