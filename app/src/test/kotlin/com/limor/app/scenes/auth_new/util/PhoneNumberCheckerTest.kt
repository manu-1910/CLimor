package com.limor.app.scenes.auth_new.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.Test

class PhoneNumberCheckerTest {

    /* POSITIVE CHECK */
    @Test
    fun checkNumberPositive() {
        val number = "+380668282100"
        val region = "UA"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assert(phoneIsCorrect)
    }

    @Test
    fun checkNumberShortNumber() {
        val number = "0668282100"
        val region = "UA"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assert(phoneIsCorrect)
    }

    @Test
    fun checkNumberShortestNumber() {
        val number = "668282100"
        val region = "UA"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assert(phoneIsCorrect)
    }

    /* NEGATIVE CHECK */
    @Test
    fun checkNumberWrongNumber() {
        val number = "+38066828210"
        val region = "UA"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assertThat(phoneIsCorrect, IsEqual(false))
    }

    @Test
    fun checkNumberWrongRegion() {
        val number = "+380668282100"
        val region = "UK"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assertThat(phoneIsCorrect, IsEqual(false))
    }

    @Test
    fun checkNumberWrongNumberAndRegion() {
        val number = "+38066828210"
        val region = "UK"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assertThat(phoneIsCorrect, IsEqual(false))
    }

    @Test
    fun checkNumberShortNumberAndWrongRegion() {
        val number = "0668282100"
        val region = "UK"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assertThat(phoneIsCorrect, IsEqual(false))
    }

    @Test
    fun checkNumberShortestNumberAndWrongRegion() {
        val number = "668282100"
        val region = "UK"
        val phoneIsCorrect = PhoneNumberChecker.checkNumber(number, region)
        assertThat(phoneIsCorrect, IsEqual(false))
    }

    /* POSITIVE FORMAT */
    private val fullNumber = "+380668282100"

    @Test
    fun getFormattedNumberPositive() {
        val number = fullNumber
        val region = "UA"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsEqual(number))
    }

    @Test
    fun getFormattedNumberShortNumber() {
        val number = "0668282100"
        val region = "UA"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsEqual(fullNumber))
    }

    @Test
    fun getFormattedNumberShortestNumber() {
        val number = "668282100"
        val region = "UA"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsEqual(fullNumber))
    }


    /* NEGATIVE FORMAT */
    @Test
    fun getFormattedNumberWrongNumber() {
        val number = "+38066828210"
        val region = "UA"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsNull())
    }

    @Test
    fun getFormattedNumberWrongRegion() {
        val number = "+380668282100"
        val region = "UK"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsNull())
    }

    @Test
    fun getFormattedNumberWrongNumberAndRegion() {
        val number = "+38066828210"
        val region = "UK"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsNull())
    }

    @Test
    fun getFormattedNumberShortNumberAndWrongRegion() {
        val number = "0668282100"
        val region = "UK"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsNull())
    }

    @Test
    fun getFormattedNumberShortestNumberAndWrongRegion() {
        val number = "668282100"
        val region = "UK"
        val phone = PhoneNumberChecker.getFormattedNumber(number, region)
        assertThat(phone, IsNull())
    }
}