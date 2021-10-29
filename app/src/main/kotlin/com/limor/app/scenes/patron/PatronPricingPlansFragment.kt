package com.limor.app.scenes.patron

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.limor.app.R
import com.limor.app.databinding.FragmentPatronPricingPlansBinding
import com.limor.app.scenes.main.fragments.profile.ShortPagerAdapter
import com.limor.app.scenes.main.viewmodels.PublishCategoriesViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PatronPricingPlansFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PatronPricingPlansFragment : Fragment(), PricingPlansAdapter.OnPlanClickListener {
    private lateinit var adapter: PricingPlansAdapter
    private var selectedSku: SkuDetails? = null
    private lateinit var billingClient: BillingClient
    private lateinit var binding: FragmentPatronPricingPlansBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishViewModel by activityViewModels { viewModelFactory }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val purchasesUpdatedListener by lazy {
        PurchasesUpdatedListener { billingResult, purchases ->
            //This is called once there is some update about purchase after launching the billing flow
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        handlePurchase(purchase!!)
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                binding.root.snackbar("Cancelled")
            } else {
                // Handle any other error codes.
            }
        }

    }

    private suspend fun handlePurchase(purchase: Purchase) {
        //TODO Update the purchased details to the backend

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        Timber.d("PURCHASE ${purchase.purchaseToken}")
        Timber.d("PURCHASE ${purchase.packageName}")

        model.consumePurchasedSub(purchase).collect {
            if (it == "Success") {
                findNavController().navigate(R.id.action_patronPricingPlansFragment_to_fragmentPatronCategories)
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPatronPricingPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setBillingClient()
        startConnectingToClient()

        binding.continueButton.setOnClickListener {
            // findNavController().navigate(R.id.action_patronPricingPlansFragment_to_fragmentPatronCategories)

            selectedSku?.let {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()
                Timber.d("In App Purchases Details ->  $it")
                val responseCode = billingClient.launchBillingFlow(requireContext() as Activity,
                    flowParams).responseCode

                Timber.d("In App Purchases Details ->  $responseCode")
            }

        }

        binding.accCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.continueButton.isEnabled = isChecked && binding.termsCheckBox.isChecked
        }
        binding.termsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.continueButton.isEnabled = isChecked && binding.accCheckBox.isChecked
        }
    }

    private fun startConnectingToClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Timber.d("Billing Result -> ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    lifecycleScope.launch {
                        querySkuDetails()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.


            }
        })
    }

    private fun setBillingClient() {
        billingClient = BillingClient.newBuilder(requireActivity())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    suspend fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("com.limor.annual_plan")
        skuList.add("monthly_plan_199")
        skuList.add("com.limor.quarterly_plan")


        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        // leverage querySkuDetails Kotlin extension function
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        Timber.d("Billing SKUs-> ${skuDetailsResult.skuDetailsList}")

        skuDetailsResult.skuDetailsList?.let {
            if (it.isNotEmpty()) {
                binding.patronPlansRV.layoutManager = LinearLayoutManager(requireContext())
                adapter = PricingPlansAdapter(it, this)
                binding.patronPlansRV.adapter = adapter
            }
        }
    }

    private fun setupViewPager() {
        val items: ArrayList<FragmentShortItemSlider> = getAdapterItems()
        binding.pager.adapter = ShortPagerAdapter(items, childFragmentManager, lifecycle)
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getAdapterItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,
            R.drawable.ic_patron_welcome)
        val item2 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,
            R.drawable.ic_patron_welcome)
        val item3 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,
            R.drawable.ic_patron_welcome)
        return arrayListOf(item1, item2, item3)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PatronPricingPlansFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onUserClicked(item: SkuDetails, position: Int) {

        if (item.freeTrialPeriod.isNotEmpty()) {
            selectedSku = item
            adapter.selectedSku = item.sku
            adapter.notifyDataSetChanged()
        }

    }

    override fun onSelectedSkuChange(item: SkuDetails) {
        selectedSku = item
        if (item.freeTrialPeriod.isNotEmpty()) {
            val termsEnd =
                 getString(R.string.plans_terms_start) + getString(R.string.plans_terms_text)
            val termsT =
                "After free trial ends, ${item.originalPrice} \n" + Html.fromHtml(
                    termsEnd,
                    Html.FROM_HTML_MODE_COMPACT)
            binding.termsTV.text = termsT
            binding.termsTV.movementMethod = LinkMovementMethod()

            val bankText = getString(R.string.text_uk_account) +
                    Html.fromHtml(getString(R.string.patron_uk_account_learn_more),
                        Html.FROM_HTML_MODE_COMPACT)
            binding.ukAccountText.text = bankText
            binding.ukAccountText.movementMethod = LinkMovementMethod()
        }

    }


}