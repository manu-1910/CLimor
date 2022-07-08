package com.limor.app.scenes.patron

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
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
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.service.DetailsAvailableListener
import com.limor.app.service.PlayBillingHandler
import com.limor.app.util.basePrice
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class PatronPricingPlansFragment : Fragment(), PricingPlansAdapter.OnPlanClickListener,
    DetailsAvailableListener {
    private lateinit var adapter: PricingPlansAdapter
    private var selectedProduct: ProductDetails? = null
    private lateinit var billingClient: BillingClient
    private lateinit var binding: FragmentPatronPricingPlansBinding

    @Inject
    lateinit var playBillingHandler: PlayBillingHandler

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishViewModel by activityViewModels { viewModelFactory }

    private var currentCode: String? = null
    private var currentPromoSubId: String? = null
    private var currentPromoSkuDetails: ProductDetails? = null
    private val defaultSkuDetails = mutableListOf<ProductDetails>()

    enum class ViewPagerMode{
        MONTHLY,
        YEARLY
    }

    private val purchasesUpdatedListener by lazy {
        PurchasesUpdatedListener { billingResult, purchases ->

            if (BuildConfig.DEBUG) {
                println("Billing result -> $billingResult")
                if (null != purchases) {
                    for (purchase in purchases) {
                        println("Purchase: $purchase")
                    }
                }
            }

            //This is called once there is some update about purchase after launching the billing flow
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                showProgressBar()
                handlePurchase(purchases.first()!!)
                if (BuildConfig.DEBUG) {
                    println("Got ${purchases.size} purchases.")
                }
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

        if (BuildConfig.DEBUG) {
            println("Will handle purchahse: $purchase")
        }

        model.consumePurchasedSub(purchase, currentCode).observe(viewLifecycleOwner) {
            if (BuildConfig.DEBUG) {
                println("Successfully consumed purchase? -> $it, is on main -> ${Looper.getMainLooper().isCurrentThread}")
            }

            if (it == "Success") {
                navigateToCategories()
            } else {
                hideProgressBar()
            }
        }
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

        setupViewPager(ViewPagerMode.MONTHLY)
        setBillingClient()
        startConnectingToClient()

        binding.continueButton.setOnClickListener {
            // findNavController().navigate(R.id.action_patronPricingPlansFragment_to_fragmentPatronCategories)

            selectedProduct?.let { productDetails ->

                val params = BillingFlowParams.ProductDetailsParams
                    .newBuilder()
                    .setProductDetails(productDetails).also { builder ->
                        var baseOfferToken: String? = null
                        var discountToken: String? = null
                        productDetails.subscriptionOfferDetails?.forEach { offerDetail ->

                            if (BuildConfig.DEBUG) {
                                println("Processing Offer Detail with tags ${offerDetail.offerTags}")
                            }

                            if (offerDetail.offerTags.contains("discount")) {
                                discountToken = offerDetail.offerToken

                            } else if (offerDetail.offerTags.size == 1 && offerDetail.offerTags.contains("base")) {
                                baseOfferToken = offerDetail.offerToken
                            }
                        }

                        val effectiveToken = discountToken ?: baseOfferToken
                        effectiveToken?.let {
                            builder.setOfferToken(it)
                        }
                        if (BuildConfig.DEBUG) {
                            println("Set offer token: $effectiveToken. Base is $baseOfferToken, discount token is $discountToken")
                        }
                    }
                    .build()


                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(mutableListOf(params))
                    .build()

                if (BuildConfig.DEBUG) {
                    Timber.d("In App Purchases Details ->  $productDetails")
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

        binding.applyCodeButton.setOnClickListener {
            if (currentPromoSubId == null ) {
                applyCode()
            } else {
                resetPromoStatus()
                adapter.refreshItems(getExtraSkuDetails())
                setPromoUI()
            }
        }
    }

    private fun resetPromoStatus() {
        currentCode = null
        currentPromoSubId = null
        currentPromoSkuDetails = null
    }

    private fun applyCode() {
        val code = binding.promoCodeTextInput.text.toString()
        currentCode = code

        if (code.isNotEmpty()) {
            model.verifyPromoCode(code).observe(viewLifecycleOwner) { res ->
                if (res.isDiscountCodeValid) {
                    applyDiscountedSubscription(res.priceId)
                } else {
                    reportCodeInvalid(code)
                }
            }
        }
    }

    private fun applyDiscountedSubscription(subId: String?) {
        if (subId == null) {
            return
        }

        // This needs to be lowercased because the shared sub ids (also used in iOS) contain
        // uppercase letters, e.g. OFF as in com.limor.dev.annual_plan.90OFF
        //
        val normalizedId = subId.lowercase()
        currentPromoSubId = normalizedId

        if (BuildConfig.DEBUG) {
            println("Will get discounted sub $subId")
        }
        playBillingHandler.getSubscriptionPrice(normalizedId, this)
    }

    private fun reportCodeInvalid(code: String) {
        LimorDialog(layoutInflater).apply {
            setTitle(R.string.title_invalid_code)
            setMessage(R.string.message_enter_valid_code)
            addButton(android.R.string.ok, true)
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingClient.endConnection()
        playBillingHandler.removeListener(this)
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
            val skuList = arrayListOf<String>()
            skuList.addAll(skuIdList)
            var monthlyPlanId :String?
            var yearlyPlanId: String?
            if(BuildConfig.DEBUG){
                monthlyPlanId = "com.limor.dev.monthly_plan"
                yearlyPlanId = "com.limor.dev.annual_plan"
            } else {
                monthlyPlanId = "com.limor.prod.monthly_plan"
                yearlyPlanId = "com.limor.prod.annual_plan"
            }
            val monthlyPlanIndex = skuIdList.indexOf(monthlyPlanId)
            val yearlyPlanIndex = skuIdList.indexOf(yearlyPlanId)
            if(monthlyPlanIndex != -1) {
                skuList[monthlyPlanIndex] = monthlyPlanId + "_new"
            }
            if(yearlyPlanIndex != -1){
                skuList[yearlyPlanIndex] = yearlyPlanId + "_new"
            }
            if (BuildConfig.DEBUG) {
                println("SKU IDs from the backend -> $skuIdList")
            }

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(skuList.map { productId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                })
                .build()

            billingClient.queryProductDetailsAsync(params) { result, productDetails ->
                if (BuildConfig.DEBUG) {
                    println("Billing result code -> ${result.responseCode} (${result.debugMessage}")
                }
                runOnUiThread {  onProductDetails(productDetails) }
            }
        }

    }

    private fun getExtraSkuDetails(): List<ExtraProductDetails> {
        val details: MutableList<ExtraProductDetails> = mutableListOf<ExtraProductDetails>()
        for (product in defaultSkuDetails) {
            if (product.productId.contains("annual")) {
                details.add(ExtraProductDetails(product, currentPromoSkuDetails))
            } else {
                details.add(ExtraProductDetails(product))
            }
        }

        return details
    }

    private fun onProductDetails(skuDetailsList: List<ProductDetails>?) {
        if (BuildConfig.DEBUG) {
            Timber.d("Billing SKUs-> $skuDetailsList")
        }
        defaultSkuDetails.clear()
        skuDetailsList?.let {
            defaultSkuDetails.add(it[1])
            defaultSkuDetails.add(it[0])
        }
        if (skuDetailsList?.isNotEmpty() == true) {
            binding.patronPlansRV.layoutManager = LinearLayoutManager(requireContext())
            adapter = PricingPlansAdapter(getExtraSkuDetails(), this)
            binding.patronPlansRV.adapter = adapter
        } else {
            binding.continueButton.visibility = View.GONE
            binding.root.snackbar(getString(R.string.no_plans_found_message))
        }

        binding.checkLayout.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun setupViewPager(mode: ViewPagerMode) {
        val items: ArrayList<FragmentShortItemSlider> = getAdapterItems(mode)
        binding.pager.adapter = ShortPagerAdapter(items, childFragmentManager, lifecycle)
        binding.indicator.setViewPager2(binding.pager)
    }

    private fun getAdapterItems(mode: ViewPagerMode): ArrayList<FragmentShortItemSlider> {
        val item1 = if(mode == ViewPagerMode.MONTHLY) {
            FragmentShortItemSlider.newInstance(R.string.monthly_patron_carousel_slide_1_title,
                R.drawable.patron_carousel_slide_1_image, R.string.monthly_patron_carousel_slide_1_sub_title)
        } else{
            FragmentShortItemSlider.newInstance(R.string.yearly_patron_carousel_slide_1_title,
                R.drawable.patron_carousel_slide_1_image, R.string.yearly_patron_carousel_slide_1_sub_title)
        }
        val item2 = if(mode == ViewPagerMode.MONTHLY){
            FragmentShortItemSlider.newInstance(R.string.monthly_patron_carousel_slide_2_title,
                    R.drawable.patron_carousel_slide_2_image, R.string.monthly_patron_carousel_slide_2_sub_title)
        } else{
            FragmentShortItemSlider.newInstance(R.string.yearly_patron_carousel_slide_2_title,
                R.drawable.patron_carousel_slide_2_image, R.string.yearly_patron_carousel_slide_2_sub_title)
        }
        val item3 = if(mode == ViewPagerMode.MONTHLY){
            FragmentShortItemSlider.newInstance(R.string.monthly_patron_carousel_slide_3_title,
                R.drawable.patron_carousel_slide_3_image, R.string.monthly_patron_carousel_slide_3_sub_title)
        } else{
            FragmentShortItemSlider.newInstance(R.string.yearly_patron_carousel_slide_3_title,
                R.drawable.patron_carousel_slide_3_image, R.string.yearly_patron_carousel_slide_3_sub_title)
        }
        return arrayListOf(item1, item2, item3)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onUserClicked(item: ExtraProductDetails, position: Int) {

        //   if (item.freeTrialPeriod.isNotEmpty()) {
        selectedProduct = item.currentProductDetails
        adapter.selectedProductId = item.currentProductDetails.productId
        adapter.notifyDataSetChanged()
        onSelectedProductChange(item)
        //  }

    }

    override fun onSelectedProductChange(item: ExtraProductDetails) {
        if(item.defaultProductDetails.productId.contains("monthly")) {
            setupViewPager(ViewPagerMode.MONTHLY)
            resetPromoStatus()
            setPromoUI()
            binding.textView6.text = getString(R.string.patron)
            binding.promoCodeLayout.visibility = View.GONE
            adapter.refreshItems(getExtraSkuDetails())
        } else{
            binding.textView6.text = getString(R.string.patron_pro)
            setupViewPager(ViewPagerMode.YEARLY)
            binding.promoCodeLayout.visibility = View.VISIBLE
        }
        selectedProduct = item.currentProductDetails
        //TODO How to check if only free trial is selectable
        //if (item.freeTrialPeriod.isNotEmpty()) {
        val termsEnd = getString(R.string.plans_terms_text)
        val termsT: Spanned = HtmlCompat.fromHtml(
            "${getString(R.string.after_free_trial)} ${item.currentProductDetails.subscriptionOfferDetails.basePrice()?.formattedPrice}" + " after Introductory Period. " + termsEnd,
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

    override fun onDetailsAvailable(details: Map<String, ProductDetails>) {
        if (null == currentPromoSubId) {
            return
        }

        currentPromoSkuDetails = details[currentPromoSubId]

        if (BuildConfig.DEBUG) {
            println("Details: $currentPromoSkuDetails")
        }

        adapter.refreshItems(getExtraSkuDetails())
        setPromoUI()
    }

    private fun setPromoUI() {
        val hasApplied = currentPromoSubId != null
        binding.applyCodeButton.text = if (hasApplied) "Remove Code" else "Apply Code"
        binding.promoCodeTextInput.apply {
            isEnabled = !hasApplied
            hint = if (hasApplied) "${this.text} Applied" else getString(R.string.enter_promo_code)
            setText(if (hasApplied) "" else binding.promoCodeTextInput.text)
        }
    }


}