package com.limor.app.apollo

import com.limor.app.CategoriesQuery
import com.limor.app.GendersQuery
import com.limor.app.LanguagesQuery
import timber.log.Timber

object GeneralInfoRepository {

    suspend fun fetchCategories(): List<CategoriesQuery.Category>? {
        val query = CategoriesQuery()
        val result = Apollo.launchQuery(query)
        var categories: List<CategoriesQuery.Category?>? =
            result?.data?.categories ?: return null
        categories = categories!!.filterNotNull()
        logList(categories)
        return categories
    }

    suspend fun fetchLanguages(): List<LanguagesQuery.Language>? {
        val query = LanguagesQuery()
        val result = Apollo.launchQuery(query)
        var languages: List<LanguagesQuery.Language?>? =
            result?.data?.languages ?: return null
        languages = languages!!.filterNotNull()
        logList(languages)
        return languages
    }

    suspend fun fetchGenders(): List<GendersQuery.Gender>? {
        val query = GendersQuery()
        val result = Apollo.launchQuery(query)
        var genders: List<GendersQuery.Gender?>? =
            result?.data?.genders ?: return null
        genders = genders!!.filterNotNull()
        logList(genders)
        return genders
    }

    private fun logList(list: List<Any>) {
        list.forEach {
            Timber.d(it.toString())
        }
    }
}