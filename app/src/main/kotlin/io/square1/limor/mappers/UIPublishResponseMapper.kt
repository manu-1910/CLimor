package io.square1.limor.mappers

import entities.request.DataMetaData
import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<PublishResponseEntity>.asUIModel(): Single<UIPublishResponse> {
    return this.map { it.asUIModel() }
}


//TODO me he quedado por aquí
fun PublishResponseEntity.asUIModel(): UIPublishResponse{
    return UIPublishResponse(
        code,
        data.asUIModel(),
        message

    )
}


fun DataPublishResponseEntity.asUIModel(): UIDataPublishResponse {
    return UIDataPublishResponse(
        podcast.asUIModel()
    )
}


fun PodcastEntity.asUIModel(): UIPodcast {
    return UIPodcast(
        active,
        address,
        audio.asUIModel(),
        bookmarked,
        caption,
        created_at,
        id,
        images.asUIModel(),
        latitude,
        liked,
        links.asUIModel(),
        listened,
        longitude,
        mentions.asUIModel(),
        number_of_comments,
        number_of_likes,
        number_of_listens,
        number_of_recasts,
        recasted,
        reported,
        saved,
        sharing_url,
        tags.asUIModel(),
        title,
        updated_at,
        user.asUIModel(),
        getAllUICategory(categories)
    )
}


fun AudioEntity.asUIModel(): UIAudio{
    return UIAudio(
        audio_url,
        original_audio_url,
        duration,
        total_samples,
        total_length,
        sample_rate,
        timestamps
    )
}


fun MentionsEntity.asUIModel(): UIMentions {
    return UIMentions(
        mentions.asUIModel()
    )
}

fun ContentMentionItemArray.asUIModel() : UIContentMentionItemsArray {
    return UIContentMentionItemsArray(
        getAllContentMentions(content)
    )
}

fun ContentMentionItemEntity.asUIModel() : UIContentMentionItem {
    return  UIContentMentionItem (
        user_id,
        username,
        start_index,
        end_index
    )
}




fun TagsEntity.asUIModel(): UITags {
    return UITags(
        id,
        text,
        count,
        isSelected
    )
}

fun CategoryEntity.asUIModel(): UICategory {
    return UICategory(
        id,
        name,
        priority,
        created_at,
        updated_at
    )
}


fun DataMetaData.asUIModel(): UIMetaData {
    return UIMetaData(
        title,
        caption,
        latitude,
        longitude,
        image_url
    )
}


fun getAllUITags(entityList: ArrayList<TagsEntity>?): ArrayList<UITags> {
    val uiList = ArrayList<UITags>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllUICategory(entityList: ArrayList<CategoryEntity>?): ArrayList<UICategory> {
    val uiList = ArrayList<UICategory>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllContentMentions(entityList: ArrayList<ContentMentionItemEntity>?): ArrayList<UIContentMentionItem> {
    val uiList = ArrayList<UIContentMentionItem>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}