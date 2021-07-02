package com.limor.app.scenes.main_new.utils

import com.limor.app.FeedItemsQuery
import com.limor.app.uimodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PodcastMapper {

    companion object {
        suspend fun podcastToUIPodcast(podcast: FeedItemsQuery.Podcast): UIPodcast {
            return  withContext(Dispatchers.Default){
                UIPodcast(
                    active = podcast.active ?: false,
                    address = podcast.address,
                    audio = UIAudio(
                        audio_url = podcast.audio?.audio_url ?: "",
                        original_audio_url = podcast.audio?.original_audio_url ?: "",
                        duration = podcast.audio?.duration ?: 0,
                        total_samples = (podcast.audio?.total_samples ?: 0).toDouble() ,
                        total_length = (podcast.audio?.total_length ?: 0).toDouble()
                    ),
                    bookmarked = podcast.bookmarked ?: false,
                    caption = podcast.caption ?: "",
                    created_at = podcast.created_at?.toInt() ?: 0,
                    id = podcast.id ?: 0,
                    images = UIImages(
                        podcast.images?.large_url ?: "",
                        podcast.images?.medium_url ?: "",
                        podcast.images?.original_url ?: "",
                        podcast.images?.small_url ?: ""
                    ),
                    latitude = podcast.latitude,
                    liked = podcast.liked ?: false,
                    links = UILinks(
                        ArrayList(getLinkWebsites(podcast)),
                        ArrayList(getLinkContent(podcast)),
                        ArrayList(getLinkCaption(podcast))
                    ),
                    listened = podcast.listened ?: false,
                    longitude = podcast.longitude,
                    mentions = UIMentions(
                        UIContentMentionItemsArray(
                            ArrayList(getMentionsContent(podcast))
                        )
                    ),
                    number_of_comments = podcast.number_of_comments ?: 0,
                    number_of_likes = podcast.number_of_likes ?: 0,
                    number_of_listens = podcast.number_of_listens ?: 0,
                    number_of_recasts = podcast.number_of_recasts ?: 0,
                    recasted = podcast.recasted ?: false,
                    reported = podcast.reported ?: false,
                    saved = false, //NO OTHER OPTION
                    sharing_url = podcast.sharing_url,
                    tags = UITagsArray(ArrayList(getTagsContent(podcast))),
                    title = podcast.title ?: "",
                    updated_at = podcast.updated_at?.toInt() ?: 0,
                    user = UIUser().copy(
                        id = podcast.owner?.id ?: 0,
                        first_name = podcast.owner?.first_name,
                        last_name = podcast.owner?.last_name,
                        images = UIImages(
                            podcast.owner?.images?.large_url ?: "",
                            podcast.owner?.images?.medium_url ?: "",
                            podcast.owner?.images?.original_url ?: "",
                            podcast.owner?.images?.small_url ?: ""
                        )
                    ),
                    category = null //NO OTHER OPTION
                )
            }

        }

        private fun getLinkWebsites(podcast: FeedItemsQuery.Podcast): List<UIWebsite> =
            podcast.links?.website?.map {
                UIWebsite(
                    it?.id ?: 0,
                    it?.link ?: "",
                    it?.start_index ?: 0,
                    it?.end_index ?: 0
                )
            } ?: listOf()

        private fun getLinkCaption(podcast: FeedItemsQuery.Podcast): List<UICaption> =
            podcast.links?.caption?.map {
                UICaption(
                    it?.id ?: 0,
                    it?.link ?: "",
                    it?.start_index ?: 0,
                    it?.end_index ?: 0
                )
            } ?: listOf()

        private fun getLinkContent(podcast: FeedItemsQuery.Podcast): List<UIContent> =
            podcast.links?.content?.map {
                UIContent(
                    it?.id ?: 0,
                    it?.link ?: "",
                    it?.start_index ?: 0,
                    it?.end_index ?: 0
                )
            } ?: listOf()

        private fun getMentionsContent(podcast: FeedItemsQuery.Podcast): List<UIContentMentionItem> =
            podcast.mentions?.content?.map {
                UIContentMentionItem(
                    it?.user_id ?: 0,
                    it?.username ?: "",
                    it?.start_index ?: 0,
                    it?.end_index ?: 0
                )
            } ?: listOf()

        private fun getTagsContent(podcast: FeedItemsQuery.Podcast): List<UITags> =
            podcast.tags?.caption?.map {
                UITags(
                    it?.tag_id ?: 0,
                    it?.tag ?: "",
                    it?.start_index ?: 0,
                    false // NO VALUE FOR NOW
                )
            } ?: listOf()

        private fun wrapWithDefaultString(origin: String?): String = origin ?: ""
        private fun wrapWithDefaultBoolean(origin: Boolean?): Boolean = origin ?: false
        private fun wrapWithDefaultInt(origin: Int?): Int = origin ?: 0
        private fun wrapWithDefaultLong(origin: Long?): Long = origin ?: 0L
        private fun wrapWithDefaultDouble(origin: Double?): Double = (origin ?: 0) as Double
    }
}