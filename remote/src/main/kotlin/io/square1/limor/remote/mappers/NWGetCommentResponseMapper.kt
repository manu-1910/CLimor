package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWGetCommentsResponse>.asDataEntity(): Single<GetCommentsResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWGetCommentsResponse.asDataEntity(): GetCommentsResponseEntity {
    return GetCommentsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWCommentAudio.asDataEntity(): CommentAudioEntity {
    return CommentAudioEntity(
        url,
        duration
    )
}


fun NWCommentsArray.asDataEntity(): CommentsEntityArray {
    return CommentsEntityArray(
        getAllCommentsEntities(comments)
    )
}

fun NWComment.asDataEntity(): CommentEntity {
    return CommentEntity(
        id,
        user.asDataEntity(),
        content,
        created_at,
        updated_at,
        mentions.asDataEntity(),
        tags.asDataEntity(),
        active,
        audio.asDataEntity(),
        type,
        liked,
        number_of_likes,
        owner_id,
        owner_type,
        getAllCommentsEntities(comments),
        podcast?.asDataEntity(),
        number_of_listens,
        podcast_id,
        links.asDataEntity(),
        comment_count
    )
}





fun getAllCommentsEntities(nwList: ArrayList<NWComment>?): ArrayList<CommentEntity> {
    val entityList = ArrayList<CommentEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}