package com.limor.app.scenes.auth_new.view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.scenes.auth_new.data.Country
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class CountrySection(
 private val title: String,
 private val countries: List<Country>,
 private val onCountryClick: (Country) -> Unit
): Section(SectionParameters.builder().itemResourceId(R.layout.layout_country_details).headerResourceId(R.layout.view_header).build()) {

    override fun getContentItemsTotal(): Int {
        return countries.size
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder? {
        return view?.let { ItemViewHolder(it) }
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ItemViewHolder
        val country: Country = countries[position]
        itemHolder.countryName.text = country.name
        itemHolder.countryCode.text = "+".plus(country.code)
        itemHolder.countryEmoji.text = country.emoji
        itemHolder.rootView.setOnClickListener { v ->
            onCountryClick(country)
        }
    }

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder? {
        return view?.let { HeaderViewHolder(it) }
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        val headerHolder: HeaderViewHolder = holder as HeaderViewHolder
        headerHolder.tvTitle.setText(title)
    }

    internal class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.headingTextView)
    }

    internal class ItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(
        rootView
    ) {
        val countryName: TextView = rootView.findViewById(R.id.country_name)
        val countryEmoji: TextView = rootView.findViewById(R.id.country_emoji)
        val countryCode: TextView = rootView.findViewById(R.id.country_code)
    }
}