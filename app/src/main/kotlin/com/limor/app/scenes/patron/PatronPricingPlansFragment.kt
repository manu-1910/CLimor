package com.limor.app.scenes.patron

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.limor.app.R
import com.limor.app.databinding.FragmentPatronPricingPlansBinding
import com.limor.app.scenes.main.fragments.profile.ShortPagerAdapter
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject

class PatronPricingPlansFragment : Fragment(), PricingPlansAdapter.OnPlanClickListener {
    private lateinit var adapter: PricingPlansAdapter
    private var selectedSku: SkuDetails? = null
    private lateinit var billingClient: BillingClient
    private lateinit var binding: FragmentPatronPricingPlansBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishViewModel by activityViewModels { viewModelFactory }

    private val purchasesUpdatedListener by lazy {
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
                binding.root.snackbar("Try Again Later")
            }
        }

    }

    private suspend fun handlePurchase(purchase: Purchase) {
        // Verify the purchase.
        Timber.d("PURCHASE ${purchase.purchaseToken}")
        Timber.d("PURCHASE ${purchase.packageName}")

        model.consumePurchasedSub(purchase).collect {
            if (it == "Success") {
                lifecycleScope.launch(Dispatchers.Main){
                    findNavController().navigate(R.id.action_patronPricingPlansFragment_to_fragmentPatronCategories)
                }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
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

        binding.accCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.continueButton.isEnabled = isChecked && binding.termsCheckBox.isChecked
        }
        binding.termsCheckBox.setOnCheckedChangeListener { _, isChecked ->
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
        model.getPlans().collect {

            it?.forEach { sku ->
                skuList.add(sku!!.productId!!)
            }

            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
            params.setType(BillingClient.SkuType.SUBS)

            // leverage querySkuDetails Kotlin extension function
            val skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient.querySkuDetails(params.build())
            }

            Timber.d("Billing SKUs-> ${skuDetailsResult.skuDetailsList}")

            skuDetailsResult.skuDetailsList?.let { skuDetails ->
                if (skuDetails.isNotEmpty()) {
                    binding.patronPlansRV.layoutManager = LinearLayoutManager(requireContext())
                    adapter = PricingPlansAdapter(skuDetails, this)
                    binding.patronPlansRV.adapter = adapter
                } else {
                    binding.continueButton.visibility = View.GONE
                    binding.root.snackbar("No Plans Found")
                }
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

    @SuppressLint("NotifyDataSetChanged")
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
            val termsT: Spanned = HtmlCompat.fromHtml(
                "After free trial ends ${item.originalPrice}. \n" +termsEnd,
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.termsTV.text = termsT
            binding.termsTV.movementMethod = LinkMovementMethod.getInstance()
            val bankText: Spanned = HtmlCompat.fromHtml(getString(R.string.patron_uk_account_learn_more),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.ukAccountText.text = bankText
            binding.ukAccountText.movementMethod = LinkMovementMethod.getInstance()
        }

    }


}