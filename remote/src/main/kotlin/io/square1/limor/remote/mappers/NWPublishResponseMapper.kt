package io.square1.limor.remote.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWPublishResponse>.asDataEntity(): Single<PublishResponseEntity>? {
    return this.map { it.asDataEntity() }
}


fun NWPublishResponse.asDataEntity(): PublishResponseEntity{
    return PublishResponseEntity(
        code,
        data.asDataEntity(),
        message

    )
}

fun NWDataPublishResponse.asDataEntity(): DataPublishResponseEntity {
    return DataPublishResponseEntity(
        podcast.asDataEntity()
    )
}

fun NWPodcast.asDataEntity(): PodcastEntity {
    return PodcastEntity(
        active,
        address,
        audio.asDataEntity(),
        bookmarked,
        caption,
        created_at,
        id,
        images.asDataEntity(),
        latitude,
        liked,
        links.asDataEntity(),
        listened,
        longitude,
        mentions.asDataEntity(),
        number_of_comments,
        number_of_likes,
        number_of_listens,
        number_of_recasts,
        recasted,
        reported,
        saved,
        sharing_url,
        tags.asDataEntity(),
        title,
        updated_at,
        user.asDataEntity(),
        category?.asDataEntity()
    )
}

fun NWAudio.asDataEntity(): AudioEntity {
    return AudioEntity(
        audio_url,
        original_audio_url,
        duration,
        total_samples,
        total_length
    )
}



fun NWMentions.asDataEntity(): MentionsEntity {
    return MentionsEntity(
        mentions.asDataEntity()
    )
}

fun NWContentMentionItemsArray.asDataEntity() : ContentMentionItemArray {
    return ContentMentionItemArray(
        getAllContentMentionEntities(content)
    )
}

fun NWContentMentionItem.asDataEntity(): ContentMentionItemEntity {
    return ContentMentionItemEntity(
        user_id,
        username,
        start_index,
        end_index
    )
}

fun NWTags.asDataEntity(): TagsEntity {
    return TagsEntity(
        id,
        text,
        count,
        isSelected
    )
}

fun NWCategory.asDataEntity(): CategoryEntity{
    return CategoryEntity(
        id,
        name,
        priority,
        created_at,
        updated_at
    )
}


fun getAllTagsEntities(nwList: ArrayList<NWTags>?): ArrayList<TagsEntity> {
    val entityList = ArrayList<TagsEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}


fun getAllCategoriesEntities(nwList: ArrayList<NWCategory>?): ArrayList<CategoryEntity> {
    val entityList = ArrayList<CategoryEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}


fun getAllContentMentionEntities(nwList: ArrayList<NWContentMentionItem>?): ArrayList<ContentMentionItemEntity> {
    val entityList = ArrayList<ContentMentionItemEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}


