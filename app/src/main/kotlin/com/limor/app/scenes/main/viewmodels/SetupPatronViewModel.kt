package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.ViewModel
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronPaymentFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronTiersFragment
import javax.inject.Inject

class SetupPatronViewModel @Inject constructor() : ViewModel() {

    var selectedCurrency: SetupPatronPaymentFragment.Currency =
        SetupPatronPaymentFragment.Currency.EURO
    var castPrice = 0f
    var isPerCreationEnabled = false
    var isMonthlyEnabled = false
    var isAllowDonationsEnabled = false
    var categorySelectedName: String = ""
    var categorySelectedId: Int = 0

    var plus18Activated: Boolean = false
    var earningsVisibleActivated: Boolean = false
    var patronageVisibleActivated: Boolean = false


    var isCurrentModifyingTierNew = false
    var currentModifyingTier : SetupPatronTiersFragment.Tier? = null
    var tierSaved: Boolean = false


    var listTiers = ArrayList<SetupPatronTiersFragment.Tier>()






}