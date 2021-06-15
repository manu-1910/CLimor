package com.limor.app.scenes.auth_new.model

import android.content.res.AssetManager
import com.limor.app.scenes.auth_new.data.Country
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset

class CountriesListProvider {
    fun provideCountries(assets: AssetManager): List<Country> {
        val loadedJson = loadJSONFromAsset(assets)
        return parseCountries(loadedJson!!)
    }

    private fun loadJSONFromAsset(assets: AssetManager): String? {
        val json: String?
        json = try {
            val inputStream = assets.open("countries.txt")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
        return json
    }

    private fun parseCountries(origin: String): MutableList<Country> {
        val array = JSONArray(origin)
        val countries = mutableListOf<Country>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONArray(i)
            val country = Country(obj)
            countries.add(country)
        }
        return countries
    }
}