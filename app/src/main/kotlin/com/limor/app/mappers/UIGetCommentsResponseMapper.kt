package com.limor.app.mappers

import com.limor.app.uimodels.UIComment
import com.limor.app.uimodels.UICommentAudio
import com.limor.app.uimodels.UICommentsArray
import com.limor.app.uimodels.UIGetCommentsResponse
import entities.response.CommentAudioEntity
import entities.response.CommentEntity
import entities.response.CommentsEntityArray
import entities.response.GetCommentsResponseEntity
import io.reactivex.Single


fun Single<GetCommentsResponseEntity>.asUIModel(): Single<UIGetCommentsResponse> {
    return this.map { it.asUIModel() }
}

fun GetCommentsResponseEntity.asUIModel(): UIGetCommentsResponse {
    return UIGetCommentsResponse(
        code, message, data.asUIModel()
    )
}

fun CommentsEntityArray.asUIModel(): UICommentsArray {
    return UICommentsArray(
        getAllUIComments(comments)
    )
}

fun CommentAudioEntity.asUIModel(): UICommentAudio {
    return UICommentAudio(
        url, duration
    )
}

fun CommentEntity.asUIModel(): UIComment {
    return UIComment(
        id,
        user?.asUIModel(),
        content,
        created_at,
        updated_at,
        mentions.asUIModel(),
        tags.asUIModel(),
        active,
        audio.asUIModel(),
        type,
        liked,
        number_of_likes,
        owner_id,
        owner_type,
        getAllUIComments(comments),
        podcast?.asUIModel(),
        number_of_listens,
        podcast_id,
        links.asUIModel(),
        comment_count
    )
}


fun getAllUIComments(entityList: ArrayList<CommentEntity>?): ArrayList<UIComment> {
    val uiList = ArrayList<UIComment>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}
