package com.limor.app.scenes.patron

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
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
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.FragmentPatronPricingPlansBinding
import com.limor.app.scenes.main.fragments.profile.ShortPagerAdapter
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.runOnUiThread
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
                showProgressBar()
                handlePurchase(purchases.first()!!)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                binding.root.snackbar(getString(R.string.cancelled))
            } else {
                // Handle any other error codes.
                binding.root.snackbar(getString(R.string.try_again))
            }
        }

    }

    private fun showProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
            binding.checkLayout.visibility = View.GONE
            binding.continueButton.visibility = View.GONE
            binding.patronPlansRV.visibility = View.INVISIBLE
        }
    }

    private fun hideProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.checkLayout.visibility = View.VISIBLE
            binding.continueButton.visibility = View.VISIBLE
            binding.patronPlansRV.visibility = View.VISIBLE
        }
    }

    private fun navigateToCategories() {
        lifecycleScope.launch(Dispatchers.Main) {
            findNavController().navigate(R.id.action_patronPricingPlansFragment_to_fragmentPatronCategories)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // Verify the purchase.
        Timber.d("PURCHASE ${purchase.purchaseToken}")
        Timber.d("PURCHASE ${purchase.packageName}")
        model.consumePurchasedSub(purchase).observe(viewLifecycleOwner, {
            if (BuildConfig.DEBUG) {
                println("Successfully consumed purchase? -> $it, is on main -> ${Looper.getMainLooper().isCurrentThread}")
            }

            if (it == "Success") {
                navigateToCategories()
            } else {
                hideProgressBar()
            }
        }
        )
        /*val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        val consumeResult = withContext(Dispatchers.IO) {
            billingClient.consumePurchase(consumeParams)
        }

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
            }

        }*/


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

                if (BuildConfig.DEBUG) {
                    Timber.d("In App Purchases Details ->  $it")
                }
                billingClient.launchBillingFlow(requireContext() as Activity, flowParams)
            }

        }

        binding.accCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.continueButton.isEnabled = isChecked && binding.termsCheckBox.isChecked
        }
        binding.termsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.continueButton.isEnabled = isChecked && binding.accCheckBox.isChecked
        }

        binding.termsTV.setOnClickListener { binding.termsCheckBox.performClick() }
        binding.ukAccountText.setOnClickListener { binding.accCheckBox.performClick() }

        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingClient.endConnection()
    }

    private fun startConnectingToClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Timber.d("Billing Result -> ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    lifecycleScope.launch(Dispatchers.Main) {
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

    fun querySkuDetails() {

        binding.progressBar.visibility = View.VISIBLE
        model.getPlanIds().observe(viewLifecycleOwner) { skuIdList ->

            if (BuildConfig.DEBUG) {
                println("SKU IDs from the backend -> $skuIdList")
            }

            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuIdList)
            params.setType(BillingClient.SkuType.SUBS)

            billingClient.querySkuDetailsAsync(params.build()) { result, skuDetails ->
                if (BuildConfig.DEBUG) {
                    println("Billing result code -> ${result.responseCode} (${result.debugMessage}")
                }
                runOnUiThread {  onSkuDetails(skuDetails) }
            }
        }

    }

    private fun onSkuDetails(skuDetailsList: List<SkuDetails>?) {
        if (BuildConfig.DEBUG) {
            Timber.d("Billing SKUs-> $skuDetailsList")
        }

        if (skuDetailsList?.isNotEmpty() == true) {
            binding.patronPlansRV.layoutManager = LinearLayoutManager(requireContext())
            adapter = PricingPlansAdapter(skuDetailsList, this)
            binding.patronPlansRV.adapter = adapter
        } else {
            binding.continueButton.visibility = View.GONE
            binding.root.snackbar(getString(R.string.no_plans_found_message))
        }

        binding.checkLayout.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun setupViewPager() {
        val items: ArrayList<FragmentShortItemSlider> = getAdapterItems()
        binding.pager.adapter = ShortPagerAdapter(items, childFragmentManager, lifecycle)
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getAdapterItems(): ArrayList<FragmentShortItemSlider> {
        val item1 = FragmentShortItemSlider.newInstance(R.string.patron_carousel_slide_1_title,
            R.drawable.patron_carousel_slide_1_image, R.string.patron_carousel_slide_1_sub_title)
        val item2 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,
            R.drawable.patron_carousel_slide_2_image, R.string.patron_carousel_slide_2_sub_title)
        val item3 = FragmentShortItemSlider.newInstance(R.string.limor_patron_request,
            R.drawable.patron_carousel_slide_3_image, R.string.patron_carousel_slide_3_sub_title)
        return arrayListOf(item1, item2, item3)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onUserClicked(item: SkuDetails, position: Int) {

        //   if (item.freeTrialPeriod.isNotEmpty()) {
        selectedSku = item
        adapter.selectedSku = item.sku
        adapter.notifyDataSetChanged()
        onSelectedSkuChange(item)
        //  }

    }

    override fun onSelectedSkuChange(item: SkuDetails) {
        selectedSku = item
        //TODO How to check if only free trial is selectable
        //if (item.freeTrialPeriod.isNotEmpty()) {
        val termsEnd = getString(R.string.plans_terms_text)
        val termsT: Spanned = HtmlCompat.fromHtml(
            "${getString(R.string.after_free_trial)} ${item.originalPrice}" + termsEnd,
            HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.termsTV.text = termsT
        binding.termsTV.movementMethod = LinkMovementMethod.getInstance()
        val bankText: Spanned =
            HtmlCompat.fromHtml(getString(R.string.patron_uk_account_learn_more),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.ukAccountText.text = bankText
        binding.ukAccountText.movementMethod = LinkMovementMethod.getInstance()
        // }

    }


}