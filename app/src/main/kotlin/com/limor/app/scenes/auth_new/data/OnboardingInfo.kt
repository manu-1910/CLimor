package com.limor.app.scenes.auth_new.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingInfo(
    @DrawableRes val image: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int
)