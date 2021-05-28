package com.limor.app.apollo

import com.limor.app.apollo.interceptors.AuthInterceptor.Companion.IS_TESTING_AUTH_CASE
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class ApolloTest {

    @Before
    fun before() {
        TestTimberInstance.initTimber()
        IS_TESTING_AUTH_CASE = true
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

    @Test
    fun apolloShouldCallGendersQuery() {
        Timber.d("\napolloShouldCallGendersQuery\n")
        runBlocking {
            try {
                val genders = GeneralInfoRepository.fetchGenders()
                assert(genders != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Test
    fun apolloShouldCallCreateUserQuery() {
        Timber.d("\napolloShouldCallCreateUserQuery\n")
        val odb = "31-05-21"
        runBlocking {
            try {
                val result = UserRepository.createUser(odb)
                assert(result != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Test
    fun apolloShouldCallUpdateUserNameQuery() {
        Timber.d("\napolloShouldCallUpdateUserNameQuery\n")
        val userName = "TestUserName"
        runBlocking {
            try {
                val result = UserRepository.updateUserName(userName)
                assert(result != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Test
    fun apolloShouldCallUpdateUserInfoQuery() {
        Timber.d("\napolloShouldCallUpdateUserInfoQuery\n")
        val gender = 0
        val categories = listOf(1, 2)
        val languages = listOf("en", "fr")
        runBlocking {
            try {
                val result = UserRepository.updateUserOnboardingData(gender, categories, languages)
                assert(result != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Test
    fun apolloShouldCallUpdateFollowingUsersDataQuery() {
        Timber.d("\napolloShouldCallUpdateFollowingUsersDataQuery\n")
        val followingIds = listOf("one", "two")
        runBlocking {
            try {
                val result = UserRepository.updateFollowingUsersData(followingIds)
                assert(result != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Test
    fun apolloShouldCallGetUserOnboardingStatusQuery() {
        Timber.d("\napolloShouldCallGetUserOnboardingStatusQuery\n")
        runBlocking {
            try {
                val result = UserRepository.getUserOnboardingStatus()
                assert(result != null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}