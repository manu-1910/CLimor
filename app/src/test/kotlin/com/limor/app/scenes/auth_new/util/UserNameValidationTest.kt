package com.limor.app.scenes.auth_new.util

import com.limor.app.scenes.auth_new.model.UserInfoProvider.Companion.userNameRegExCheck
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserNameValidationTest {
    @Test
    fun shouldValidateUserName() {

        assertTrue(testStringForRegEx("Gsj"))
        assertTrue(testStringForRegEx("Gsjwqe"))
        assertTrue(testStringForRegEx("Gsjwqe_5.2"))
        assertTrue(testStringForRegEx("Gsjwqe_52_323.21"))
        assertTrue(testStringForRegEx("Jjjjjjjjjjjjjjjjjjjjjjjjjjjjjj")) //30

        assertFalse(testStringForRegEx(".Gsjda"))
        assertFalse(testStringForRegEx("_Gsjda"))
        assertFalse(testStringForRegEx("sj"))
        assertFalse(testStringForRegEx("7sj"))
        assertFalse(testStringForRegEx("Gsj."))
        assertFalse(testStringForRegEx("Gsj_"))
        assertFalse(testStringForRegEx("Gsj__1"))
        assertFalse(testStringForRegEx("Gsj..1"))
        assertFalse(testStringForRegEx("Gsj_.1"))
        assertFalse(testStringForRegEx("Gsj._1"))
        assertFalse(testStringForRegEx("Jjjjjjjjjjjjjjjjjjjjjjjjjjjjjj1"))//31
    }

    private fun testStringForRegEx(text: String): Boolean {
        return userNameRegExCheck(text)
    }
}