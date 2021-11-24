package com.limor.app.scenes.auth_new.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber


object PhoneNumberChecker {

    fun checkNumber(number: String, region: String): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()

        val phoneNumber: Phonenumber.PhoneNumber = try {
            phoneUtil.parse(number, region)
        } catch (e: Exception) {
            // println(e) no need of that
            return false
        }
        return phoneUtil.isValidNumberForRegion(phoneNumber, region)
    }

    private val phoneUtil: PhoneNumberUtil
        get() = PhoneNumberUtil.getInstance()

    fun getFormattedNumber(number: String, region: String): String? {
        if (!checkNumber(number, region)) return null
        val phoneNumber: Phonenumber.PhoneNumber = try {
            phoneUtil.parse(number, region)
        } catch (e: Exception) {
            // println(e) no need of that
            return null
        }
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
    }
}