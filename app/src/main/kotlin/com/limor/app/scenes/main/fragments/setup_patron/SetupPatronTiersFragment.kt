package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.adapters.TiersAdapter
import com.limor.app.scenes.main.viewmodels.SetupPatronViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import kotlinx.android.synthetic.main.fragment_setup_patron_tiers.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class SetupPatronTiersFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var setupPatronViewModel: SetupPatronViewModel
    private lateinit var btnAddTier: ImageButton

    private var rootView: View? = null
    var app: App? = null

    private val listTiers = ArrayList<Tier>()
    private lateinit var tiersAdapter: TiersAdapter

    companion object {
        val TAG: String = SetupPatronSettingsFragment::class.java.simpleName
        fun newInstance() = SetupPatronSettingsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.fragment_setup_patron_tiers, container, false)
            createDummyTiers()
        }

        app = context?.applicationContext as App


        return rootView
    }

    override fun onResume() {
        super.onResume()
        setupPatronViewModel.currentModifyingTier?.let {
            if(setupPatronViewModel.isCurrentModifyingTierNew) {
                listTiers.add(it)
                setupPatronViewModel.currentModifyingTier = null
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        setupToolbar()
        listeners()
        configureAdapter()
    }


    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvTiers?.layoutManager = layoutManager
        context?.let { context ->
            tiersAdapter = TiersAdapter(
                context,
                listTiers,
                object : TiersAdapter.OnTierClickedListener {
                    override fun onTierClicked(item: Tier, position: Int) {
                        toast("You clicked on tier").show()
                    }

                    override fun onEditTierClicked(item: Tier, position: Int) {
                        setupPatronViewModel.currentModifyingTier = item
                        setupPatronViewModel.isCurrentModifyingTierNew = false
                        findNavController().navigate(R.id.action_setup_patron_tiers_to_new_tier)
                    }

                    override fun onRemoveTierClicked(currentItem: Tier, position: Int) {
                        listTiers.remove(currentItem)
                        tiersAdapter.notifyItemRemoved(position)
                    }
                }
            )

        }

        rvTiers?.adapter = tiersAdapter
        rvTiers?.isNestedScrollingEnabled = true

//        layNestedScroll.setOnScrollChangeListener { v: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
//            v?.let {
////                if(v.getChildAt(v.childCount - 5) != null) {
////                    if ((scrollY >= (v.getChildAt(v.childCount - 5).measuredHeight - v.measuredHeight)) && scrollY > oldScrollY) {
//                if (!isLastPage && !isWaitingForApiCall && rvComments != null && rvComments.visibility == View.VISIBLE) {
//                    val goingDown = scrollY > oldScrollY
//                    if (goingDown && (scrollY >= (rvComments.measuredHeight - v.measuredHeight))) {
//
////                        toast("We have to scroll more")
//                        isWaitingForApiCall = true
//                        if (podcastMode) {
//                            viewModelGetPodcastComments.offset = currentOffset
//                            getPodcastCommentsDataTrigger.onNext(Unit)
//                        } else {
//                            viewModelGetCommentComments.offset = currentOffset
//                            getCommentCommentsDataTrigger.onNext(Unit)
//                        }
//                    }
//                }
//            }
//        }
        rvComments?.setHasFixedSize(true)
    }


    private fun createDummyTiers() {
        val benefits = "• Premium Casts\\n• Access to Private DM"
        val tier1 = Tier("Limor Patron Tier 1", benefits, 5.0f)
        val tier2 = Tier("Limor Patron Tier 2", benefits, 50.0f)
        val tier3 = Tier("Limor Patron Tier 3", benefits, 100.0f)
        listTiers.add(tier1)
        listTiers.add(tier2)
        listTiers.add(tier3)
    }


    private fun setupToolbar() {
        val tvToolbarTitle = activity?.findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.title_tiers)
        btnAddTier = activity?.findViewById(R.id.btnInfo)!!
        btnAddTier.imageResource = R.drawable.add_yellow
        btnAddTier.visibility = View.VISIBLE
    }


    private fun bindViewModel() {
        activity?.let {
            setupPatronViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(SetupPatronViewModel::class.java)
        }
    }

    private fun listeners() {
        btnAddTier.onClick {
            setupPatronViewModel.currentModifyingTier = Tier()
            setupPatronViewModel.isCurrentModifyingTierNew = true
            findNavController().navigate(R.id.action_setup_patron_tiers_to_new_tier)
        }
    }


    class Tier(
        var name: String = "",
        var benefits: String = "",
        var price: Float = 0f,
        var currency: SetupPatronPaymentFragment.Currency = SetupPatronPaymentFragment.Currency.EURO
    ) {

        val id: Int = currentId++

        companion object {
            private var currentId = 0
        }
    }

}