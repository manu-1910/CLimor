package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LinkUIModel(
    val website: List<LinkDataUIModel>,
    val content: List<LinkDataUIModel>,
    val caption: List<LinkDataUIModel>,
) : Parcelable {

    @Parcelize
    data class LinkDataUIModel(
        val id: Int,
        val link: String,
        val startIndex: Int,
        val endIndex: Int
    ) : Parcelable
}

fun GetFeaturedCastsQuery.Links.mapToUIModel() =
    LinkUIModel(
        website = website!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        content = content!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
    )

fun GetTopCastsQuery.Links.mapToUIModel() =
    LinkUIModel(
        website = website!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        content = content!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
    )

fun GetPodcastsByCategoryQuery.Links.mapToUIModel() =
    LinkUIModel(
        website = website!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        content = content!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
    )

fun GetPodcastsByHashtagQuery.Links.mapToUIModel() =
    LinkUIModel(
        website = website!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        content = content!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
    )

fun GetUserPodcastsQuery.Links.mapToUIModel() =
    LinkUIModel(
        website = website!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        content = content!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            LinkUIModel.LinkDataUIModel(
                id = it!!.id!!,
                link = it.link!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
    )
