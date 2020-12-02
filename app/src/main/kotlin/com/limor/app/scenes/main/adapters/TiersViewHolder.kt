package com.limor.app.scenes.main.adapters

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronPaymentFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronTiersFragment
import org.jetbrains.anko.sdk23.listeners.onClick


class TiersViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val tierClickedListener: TiersAdapter.OnTierClickedListener,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_tier_item_recycler_view,
        parent,
        false
    )
) {

    private var tvTierBenefits: TextView = itemView.findViewById(R.id.tvTierBenefits)
    private var tvTierName: TextView = itemView.findViewById(R.id.tvTierName)
    private var tvTierPrice: TextView = itemView.findViewById(R.id.tvTierPrice)
    private var tvEditTier: TextView = itemView.findViewById(R.id.tvEditTier)
    private var tvRemoveTier: TextView = itemView.findViewById(R.id.tvRemoveTier)

    fun bind(currentItem: SetupPatronTiersFragment.Tier, position: Int) {

        tvEditTier.onClick { tierClickedListener.onEditTierClicked(currentItem, position) }
        tvRemoveTier.onClick { tierClickedListener.onRemoveTierClicked(currentItem, position) }

        tvTierBenefits.text = currentItem.benefits

        val pieces = currentItem.name.split(" ", ignoreCase = true, limit = 2)
        if(pieces.size <= 1) {
            tvTierName.text = currentItem.name
        } else {
            val firstWord = pieces[0]
            val restOfText = " ${pieces[1]}"
            val restOfTextYellow = SpannableString(restOfText)
            restOfTextYellow.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.brandPrimary500)),
                0,
                restOfText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            tvTierName.text = firstWord
            tvTierName.append(restOfTextYellow)
        }


        val currencyChar = getCurrencyChar(currentItem.currency)
        val twoDigitsStringPrice = String.format("%.2f", currentItem.price)
        val priceText = "$currencyChar $twoDigitsStringPrice"
        tvTierPrice.text = priceText
    }

    private fun getCurrencyChar(currency: SetupPatronPaymentFragment.Currency): String {
        return when (currency) {
            SetupPatronPaymentFragment.Currency.EURO -> "€"
            SetupPatronPaymentFragment.Currency.DOLLAR -> "$"
            SetupPatronPaymentFragment.Currency.POUND -> "£"
        }
    }


}