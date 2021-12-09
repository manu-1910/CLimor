package com.limor.app.scenes.patron.manage.fragment

import android.R
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.SkuDetails
import com.limor.app.databinding.ActivityChangePriceBinding
import com.limor.app.scenes.patron.viewmodels.CastPriceViewModel
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.service.PlayBillingHandler
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_change_price.*
import javax.inject.Inject


class ChangePriceActivity : AppCompatActivity() {

    companion object {
        const val CHANGE_PRICE_FOR_ALL_CASTS = "CHANGE_PRICE_FOR_ALL_CASTS"
        const val CAST_ID = "CAST_ID"
        const val TAG = "CHANGE_PRICE_ACTIVITY"
        const val PRICE_ID = "PRICE_ID"
        const val SELECTED_PRICE_ID = "SELECTED_PRICE_ID"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: CastPriceViewModel by viewModels { viewModelFactory }
    private lateinit var binding: ActivityChangePriceBinding

    @Inject
    lateinit var playBillingHandler: PlayBillingHandler

    private var changePriceForAllCasts: Boolean = false
    private var castId: Int = 0
    private var selectedPriceId = ""
    private var priceId: String = ""
    private val details = mutableMapOf<String, SkuDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        binding = ActivityChangePriceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeViewModel()
    }

    override fun onStart() {
        super.onStart()
        setOnClicks()
        //setPrices()
        initialiseViews()
    }

    private fun setOnClicks() {
        binding.yesButton.setOnClickListener {
            LimorDialog(layoutInflater).apply {
                setTitle(
                    if (changePriceForAllCasts) resources.getString(com.limor.app.R.string.change_cast_prices) else resources.getString(
                        com.limor.app.R.string.change_price
                    )
                )
                setMessage(
                    if (changePriceForAllCasts) resources.getString(com.limor.app.R.string.change_price_confirmation_text) else resources.getString(
                        com.limor.app.R.string.change_cast_price_confirmation_desc
                    )
                )
                setMessageColor(
                    ContextCompat.getColor(
                        it.context,
                        com.limor.app.R.color.textSecondary
                    )
                )
                setIcon(com.limor.app.R.drawable.ic_change_price)
                addButton(com.limor.app.R.string.continue_button, false) { performUpdate() }
                addButton(com.limor.app.R.string.cancel, true)
            }.show()
        }
        binding.toolbar.btnBack.setOnClickListener {
            finish()
            setResult(Activity.RESULT_CANCELED)
        }
    }

    private fun performUpdate() {
        if (changePriceForAllCasts) {
            model.updateAllCastsPrice(priceId)
        } else {
            model.updatePriceForACast(castId, priceId)
        }
    }

    private fun setPrices(list: ArrayList<String>, position: Int) {
        val editText = priceTIL.editText as AutoCompleteTextView

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(
                this,
                R.layout.select_dialog_item,
                list
            )
        editText.setAdapter(adapter)

        editText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, pos, id ->
                binding.yesButton.isEnabled = true
                priceId = details[list[pos]]?.sku ?: ""
            }

        if(position != -1){
            binding.tvPrices.setText(details[list[position]]?.price, false)
            binding.yesButton.isEnabled = true
            priceId = details[list[position]]?.sku ?: ""
        }
    }

    private fun subscribeViewModel() {
        model.priceUpdated.observe(this, {
            if (it == true) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
        var list = ArrayList<String>()
        var selectedPos = -1
        playBillingHandler.getPrices().observe(this, { skuList ->
            details.putAll(skuList.map { skuDetails -> skuDetails.originalPrice to skuDetails })
            skuList.forEachIndexed { i, skuDetails ->
                selectedPos = if(skuDetails.sku == selectedPriceId){ i } else { -1 }
                list.add(skuDetails.price)
            }
            setPrices(list, selectedPos)
        })
    }

    private fun initialiseViews() {
        changePriceForAllCasts = intent.getBooleanExtra(CHANGE_PRICE_FOR_ALL_CASTS, false)
        castId = intent.getIntExtra(CAST_ID, -1)
        selectedPriceId = intent.getStringExtra(SELECTED_PRICE_ID) ?: ""
        binding.toolbar.title.text = getString(com.limor.app.R.string.edit_price_text)
        binding.toolbar.btnNotification.setImageDrawable(resources.getDrawable(R.drawable.ic_menu_info_details))
        binding.yesButton.isEnabled = false
    }

}