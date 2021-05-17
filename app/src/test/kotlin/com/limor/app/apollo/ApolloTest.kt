package com.limor.app.apollo

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class ApolloTest {

    @Before
    fun before() {
        TestTimberInstance.initTimber()
    }

    @Test
    fun apolloShouldCallCategoriesQuery() {
        Timber.d("\napolloShouldCallCategoriesQuery\n")
        runBlocking {
            try {
                val categories = GeneralInfoRepository.fetchCategories()
                assert(categories != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Test
    fun apolloShouldCallLanguagesQuery() {
        Timber.d("\napolloShouldCallLanguagesQuery\n")
        runBlocking {
            try {
                val languages = GeneralInfoRepository.fetchLanguages()
                assert(languages != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}