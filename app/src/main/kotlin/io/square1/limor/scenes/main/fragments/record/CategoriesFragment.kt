package io.square1.limor.scenes.main.fragments.record

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.viewmodels.CategoriesViewModel
import io.square1.limor.scenes.main.viewmodels.PublishViewModel
import io.square1.limor.uimodels.UICategory
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import org.jetbrains.anko.okButton
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import javax.inject.Inject


class CategoriesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var publishViewModel: PublishViewModel

    private var rvCategories: RecyclerView? = null
    private var rootView: View? = null
    private var listCategories = ArrayList<UICategory>()
    private var listCategoriesSelected = ArrayList<UICategory>()
    private val categoriesTrigger = PublishSubject.create<Unit>()

    private var chipGroup: ChipGroup? = null

    var app: App? = null



    companion object {
        val TAG: String = CategoriesFragment::class.java.simpleName
        fun newInstance() = CategoriesFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_categories, container, false)
        }
        chipGroup = rootView!!.findViewById(R.id.categoryChipsView)
        app = context?.applicationContext as App
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()
        apiCallGetCategories()
        categoriesListener()

        //setupRecycler(listCategories)


    }

    override fun onResume() {
        super.onResume()

        categoriesTrigger.onNext(Unit)
    }


    private fun bindViewModel() {
        activity?.let {
            categoriesViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(CategoriesViewModel::class.java)

            publishViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(PublishViewModel::class.java)
        }
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_category)

        //Toolbar Left
        btnClose.onClick {
            categoriesViewModel.localListCategoriesSelected.clear()
            categoriesViewModel.localListCategories.clear()

            categoriesViewModel.localListCategories.addAll(listCategories)
            categoriesViewModel.localListCategoriesSelected.addAll(listCategoriesSelected)
            findNavController().popBackStack()
        }

    }


    private fun apiCallGetCategories() {
        val output = categoriesViewModel.transform(
            CategoriesViewModel.Input(
                categoriesTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Tags Response Ok
                /*
                     <com.google.android.material.chip.Chip
                android:id="@+id/chip"
                style="@style/Widget.MaterialComponents.Chip.Filter"
                android:textAppearance="@style/TextAppearance.MaterialComponents.Button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="10dp"
                android:text="Sport"
                android:textColor="@android:color/white"
                app:checkedIcon="@drawable/ic_select"
                app:chipBackgroundColor="@color/colorPrimary"
                app:chipEndPadding="8dp"
                app:chipIconTint="@android:color/white"
                app:chipStartPadding="@dimen/marginSmall"
                app:textEndPadding="@dimen/marginSmall"
                app:textStartPadding="@dimen/marginSmall"
                app:chipCornerRadius="6dp"
                app:chipStrokeWidth="1dp"
                app:chipStrokeColor="@color/brandSecondary100"/>
                * */

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(16, 16, 16, 16)



                val states = arrayOf(
                    intArrayOf(android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_pressed)
                )

                val colors = intArrayOf(
                    Color.BLACK,
                    Color.RED,
                    Color.GREEN,
                    Color.BLUE
                )

                val myList = ColorStateList(states, colors)

                for (item in it.data.categories) {

//                    val categoryChip = Chip(context)
//                    categoryChip.id = item.id
//                    categoryChip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Button)
//                    //style="@style/Widget.MaterialComponents.Chip.Filter"
//                    categoryChip.style { R.style.Widget_MaterialComponents_Chip_Filter }
//
//                    categoryChip.text = item.name
//                    categoryChip.textColor = ContextCompat.getColor(context!!, R.color.white)
//                    categoryChip.backgroundColor = ContextCompat.getColor(
//                        context!!,
//                        R.color.colorPrimary
//                    )
//                    categoryChip.chipCornerRadius = 6f
//                    categoryChip.chipStrokeWidth = 1f
//                    categoryChip.chipStrokeColor = myList

                    val tvChip = TextView(context)
                    tvChip.id = item.id
                    tvChip.text = item.name
                    tvChip.setTextColor(ContextCompat.getColorStateList(context!!, R.color.chip_textcolor))
                    tvChip.isEnabled = true
                    tvChip.padding = 24
                    tvChip.background = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.bg_chip_category
                    )
                    tvChip.layoutParams = params
                    tvChip.setOnClickListener {
                        publishViewModel.categorySelected = tvChip.text.toString()
                        findNavController().popBackStack()
                    }

                    chipGroup!!.addView(tvChip)
                }
                toast("success")

            }


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

                if (it.code == 10) {  //Session expired
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
                } else {
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


    private fun categoriesListener(){



//        var i=1
//        val genres = arrayOf("Thriller 2", "Comedy 2", "Adventure 2")
//        for (genre in genres) {
//
//            var temp : Chip = Chip(context, null, R.style.Widget_MaterialComponents_Chip_Filter)
//            temp.id=i
//            temp.text= genre+i
//
//
//            if (Build.VERSION.SDK_INT < 23) {
//                temp.setTextAppearance(context, R.style.TextAppearance_MaterialComponents_Button)
//            } else{
//                temp.setTextAppearance(R.style.TextAppearance_MaterialComponents_Button)
//            }
//
//            chipGroup!!.addView(temp)
//            i++
//        }








    }



}

