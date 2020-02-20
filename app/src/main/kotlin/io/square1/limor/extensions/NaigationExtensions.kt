package io.square1.limor.extensions

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.square1.limor.common.BaseFragment

fun FragmentActivity.navigateToActivityRemovingPrevious(classToStartIntent: Class<*>) =
    this.navigateToActivityRemovingPrevious(classToStartIntent, null)

fun FragmentActivity.navigateToActivityRemovingPrevious(classToStartIntent: Class<*>, extras: Bundle?) {
    val intent = Intent(this.applicationContext, classToStartIntent)
    if (extras != null) {
        intent.putExtras(extras)
    }
    this.startActivity(intent)
    this.finish()
}

fun FragmentActivity.navigateToActivityRemovingPreviousWithExtrasClearTop(
    classToStartIntent: Class<*>, extras: Bundle) {
    this.navigateToActivityWithExtrasClearTop(classToStartIntent, extras, false, 0, 0)
    this.finish()
}

fun FragmentActivity.navigateToActivityRemovingPreviousWithExtrasClearTopAndAnimation(
    classToStartIntent: Class<*>, extras: Bundle, inTransition: Int, outTransition: Int) {
    this.navigateToActivityWithExtrasClearTop(classToStartIntent, extras, true, inTransition, outTransition)
    this.finish()
}

fun FragmentActivity.navigateToActivity(classToStartIntent: Class<*>) =
    this.navigateToActivity(classToStartIntent, null, false, 0, 0)

fun FragmentActivity.navigateToActivityWithAnimation(classToStartIntent: Class<*>,
                                                     inTransition: Int, outTransition: Int) =
    this.navigateToActivity(classToStartIntent, null, true, inTransition, outTransition)

fun FragmentActivity.navigateToActivity(classToStartIntent: Class<*>,
                                        extras: Bundle?) =
    this.navigateToActivity(classToStartIntent, extras, false, 0, 0)


fun FragmentActivity.navigateToActivity(classToStartIntent: Class<*>,
                                        extras: Bundle?, haveTransition: Boolean,
                                        inTransition: Int, outTransition: Int) {
    val intent = Intent(this.applicationContext, classToStartIntent)

    if (extras != null) {
        intent.putExtras(extras)
    }
    this.startActivity(intent)
    if (haveTransition) {
        this.setActivityTransitionAnimation(inTransition, outTransition)
    }
}

fun FragmentActivity.navigateToActivityWithExtrasClearTop(classToStartIntent: Class<*>, extras: Bundle?,
                                                          haveTransition: Boolean,
                                                          inTransition: Int, outTransition: Int) {
    val intent = Intent(this.applicationContext, classToStartIntent)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    if (extras != null) {
        intent.putExtras(extras)
    }
    this.startActivity(intent)
    if (haveTransition) {
        this.setActivityTransitionAnimation(inTransition, outTransition)
    }
}

fun FragmentActivity.navigateToActivityRemovingPreviousClearTop(classToStartIntent: Class<*>,
                                                                haveTransition: Boolean, inTransition: Int, outTransition: Int) {
    val intent = Intent(this.applicationContext, classToStartIntent)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    this.startActivity(intent)
    if (haveTransition) {
        this.setActivityTransitionAnimation(inTransition, outTransition)
    }
    this.finish()
}

fun FragmentActivity.setActivityTransitionAnimation(inTransition: Int, outTransition: Int) =
    this.overridePendingTransition(inTransition, outTransition)

fun FragmentActivity.isFragmentAlreadyInForeground(fragmentTag: String): Boolean {
    val foundFragment = this.supportFragmentManager
        .findFragmentByTag(fragmentTag)

    return foundFragment != null && foundFragment.isVisible
}

fun FragmentActivity.getBackStackSize(): Int = this.supportFragmentManager.backStackEntryCount

fun FragmentActivity.navigateToActivityForResult(classToStartIntent: Class<*>, extras: Bundle?, requestCode: Int) =
    this.navigateToActivityForResult(classToStartIntent,extras,requestCode,false,0,0)

fun FragmentActivity.navigateToActivityForResult(classToStartIntent: Class<*>, extras: Bundle?,
                                                 requestCode: Int, haveTransition: Boolean,
                                                 inTransition: Int, outTransition: Int) {
    val intent = Intent(this.applicationContext, classToStartIntent)
    if (extras != null) {
        intent.putExtras(extras)
    }
    this.startActivityForResult(intent, requestCode)
    if (haveTransition) {
        this.setActivityTransitionAnimation(inTransition,outTransition)
    }
}

fun FragmentActivity.navigateToFragment(fragment: Fragment, contentFrame: Int, addToBackStack: Boolean) {
    this.pushFragment(fragment, contentFrame, addToBackStack, 0, 0, 0, 0)
}

fun FragmentActivity.navigateToFragment(fragment: Fragment, contentFrame: Int, addToBackStack: Boolean,
                                        anim: Int, anim2: Int, animOut: Int, animOut2: Int) {
    this.pushFragment(fragment, contentFrame, addToBackStack, anim, anim2, animOut, animOut2)
}

fun FragmentActivity.navigateToFragment(fragment: Fragment, contentFrame: Int, addToBackStack: Boolean, anim: Int, anim2: Int) {
    this.pushFragment(fragment, contentFrame, addToBackStack, anim, anim2, 0, 0)
}

fun FragmentActivity.navigateToFragmentCleanStack(fragment: Fragment, contentFrame: Int, addToBackStack: Boolean) {
    this.cleanBackStack()
    this.pushFragment(fragment, contentFrame, addToBackStack, 0, 0, 0, 0)
}

fun FragmentActivity.navigateToFragmentCleanStack(fragment: Fragment, contentFrame: Int, addToBackStack: Boolean,
                                                  anim: Int, anim2: Int, animOut: Int, animOut2: Int) {
    this.cleanBackStack()
    this.pushFragment(fragment, contentFrame, addToBackStack, anim, anim2, animOut, animOut2)
}

fun FragmentActivity.navigateToFragmentCleanStack(fragment: Fragment, contentFrame: Int,addToBackStack: Boolean,
                                                  anim: Int, anim2: Int) {
    this.cleanBackStack()
    this.pushFragment(fragment, contentFrame, addToBackStack, anim, anim2, 0, 0)
}

fun FragmentActivity.cleanBackStack(viewStringId: String) {
    this.supportFragmentManager.popBackStackImmediate(viewStringId,
        FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun FragmentActivity.cleanBackStack() {
    this.supportFragmentManager.popBackStackImmediate(null,
        FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun FragmentActivity.removeCurrentFragment(fragment: Fragment) {
    val fragmentManager = this.supportFragmentManager
    val transaction = fragmentManager.beginTransaction()
    transaction.remove(fragment)
    transaction.commit()
}

fun FragmentActivity.pushFragment(fragment: Fragment ,contentFrame: Int, addToBackStack: Boolean,
                                  anim: Int, anim2: Int, animOut: Int, animOut2: Int) {
    val fragmentTransaction = this.supportFragmentManager
        .beginTransaction()
    if (anim > 0 && anim2 > 0) {
        if (animOut > 0 && animOut2 > 0) {
            fragmentTransaction.setCustomAnimations(anim, anim2, animOut, animOut2)
        } else {
            fragmentTransaction.setCustomAnimations(anim, anim2)
        }
    }


    val tag: String? = if (fragment is BaseFragment) {
        fragment.fragmentId
    } else {
        (fragment as Any).javaClass.name
    }

    fragmentTransaction.replace(contentFrame, fragment, tag)

    if (addToBackStack) {
        fragmentTransaction.addToBackStack(tag)
    }

    fragmentTransaction.commit()
    if (contentFrame <= 0) {
        this.supportFragmentManager.executePendingTransactions()
    }
}