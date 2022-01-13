package com.limor.app.playlists.models

import com.limor.app.GetPlaylistsOfCastsQuery

data class PlaylistImages(
    val smallUrl: String,
    val mediumUrl: String,
    val largeUrl: String,
    val originalUrl: String
) {
    companion object {
        private val dummyImages = listOf(
            PlaylistImages(
                smallUrl = "https://limor-platform-production.s3.amazonaws.com/users/images/000/007/687/small/user_image_2ca01ffba13ff57c2e2a66cffbbbcd97.png",
                mediumUrl = "https://limor-platform-production.s3.amazonaws.com/users/images/000/007/687/medium/user_image_2ca01ffba13ff57c2e2a66cffbbbcd97.png",
                largeUrl = "https://limor-platform-production.s3.amazonaws.com/users/images/000/007/687/large/user_image_2ca01ffba13ff57c2e2a66cffbbbcd97.png",
                originalUrl = "https://limor-platform-production.s3.amazonaws.com/users/images/000/007/687/original/user_image_2ca01ffba13ff57c2e2a66cffbbbcd97.png"
            ),
            PlaylistImages(
                smallUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/083/small/podcast_image_a92b4f4d522f371035ad2315e82c61eb.png",
                mediumUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/083/medium/podcast_image_a92b4f4d522f371035ad2315e82c61eb.png",
                largeUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/083/large/podcast_image_a92b4f4d522f371035ad2315e82c61eb.png",
                originalUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/083/original/podcast_image_a92b4f4d522f371035ad2315e82c61eb.png"
            ),
            PlaylistImages(
                smallUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/075/small/podcast_image_e6d7cbdeb399c41b5136d17fc6b3fba2.png",
                mediumUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/075/medium/podcast_image_e6d7cbdeb399c41b5136d17fc6b3fba2.png",
                largeUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/075/large/podcast_image_e6d7cbdeb399c41b5136d17fc6b3fba2.png",
                originalUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/017/075/original/podcast_image_e6d7cbdeb399c41b5136d17fc6b3fba2.png"
            ),
            PlaylistImages(
                smallUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/824/small/APEC_Power_ep10_R_Sweeney.jpg",
                mediumUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/824/medium/APEC_Power_ep10_R_Sweeney.jpg",
                largeUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/824/large/APEC_Power_ep10_R_Sweeney.jpg",
                originalUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/824/original/APEC_Power_ep10_R_Sweeney.jpg"
            ),
            PlaylistImages(
                smallUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/724/small/APEC_Power_ep8_Shane_M.jpg",
                mediumUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/724/medium/APEC_Power_ep8_Shane_M.jpg",
                largeUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/724/large/APEC_Power_ep8_Shane_M.jpg",
                originalUrl = "https://limor-platform-production.s3.amazonaws.com/podcasts/images/000/016/724/original/APEC_Power_ep8_Shane_M.jpg"
            )
        )

        fun dummyAt(index: Int): PlaylistImages {
            return dummyImages[index]
        }

        fun dummy(): PlaylistImages {
            return dummyImages.random()
        }
    }
}

data class PlaylistUIModel(
    val id: Int,
    val title: String,
    val images: PlaylistImages? = null,
    val colorCode: String? = null,
    val isCustom: Boolean,
    val count: Int,
    var isAdded: Boolean,
    var isPublic: Boolean
) {
    companion object {
        fun dummyList(ownCount: Int): List<PlaylistUIModel> {
            val list = mutableListOf<PlaylistUIModel>()

            val purchasedImages = listOf(null, PlaylistImages.dummyAt(0)).random()
            list.add(
                PlaylistUIModel(
                    id = 0,
                    title = "Purchased casts",
                    images = purchasedImages,
                    isCustom = false,
                    count = if (purchasedImages == null) 0 else 10,
                    isAdded = false,
                    isPublic = false
                )
            )

            val likedImages = listOf(null, PlaylistImages.dummyAt(1)).random()
            list.add(
                PlaylistUIModel(
                    id = 1,
                    title = "Liked casts",
                    images = likedImages,
                    isCustom = false,
                    count = if (likedImages == null) 0 else 10,
                    isAdded = false,
                    isPublic = false
                )
            )

            for (i in 1..ownCount) {
                val images = listOf(null, PlaylistImages.dummy()).random()
                val colorCode = if (images == null) listOf(null, "#FF0000", "#00FF00", "#0000FF").random() else null
                list.add(
                    PlaylistUIModel(
                        id = 1 + i,
                        title = listOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten").random(),
                        images = images,
                        colorCode = colorCode,
                        isCustom = true,
                        count = if (images == null && colorCode == null) 0 else 15,
                        isAdded = false,
                        isPublic = false
                    )
                )
            }
            return list
        }
    }
}

fun GetPlaylistsOfCastsQuery.Data1.mapToUIModel() =
    PlaylistUIModel(
        id = playlistId ?: -1,
        title = title ?: "",
        images = images?.mapToUIModel(),
        colorCode = colorCode,
        isCustom = isCustom ?: false,
        count = count ?: 0,
        isAdded = isAdded ?: false,
        isPublic = isPublic ?: false
    )

fun GetPlaylistsOfCastsQuery.Images.mapToUIModel() =
    PlaylistImages(
        smallUrl = small_url ?: "",
        mediumUrl = medium_url ?: "",
        largeUrl = large_url ?: "",
        originalUrl = original_url ?: ""
    )