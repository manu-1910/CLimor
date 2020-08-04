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
        user.asUIModel()
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
    )
}


fun TagsEntity.asUIModel(): UITags {
    return UITags(
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
