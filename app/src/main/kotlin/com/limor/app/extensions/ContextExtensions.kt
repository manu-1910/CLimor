package com.limor.app.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

tailrec fun Context.getActivity(): Activity? = this as? Activity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()