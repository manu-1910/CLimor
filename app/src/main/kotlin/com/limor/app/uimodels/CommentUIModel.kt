package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.extensions.toLocalDateTime
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Parcelize
data class CommentUIModel(
    val id: Int,
    val user: UserUIModel?,
    val content: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val mentions: MentionUIModel?,
    val tags: List<TagUIModel>?,
    val isActive: Boolean?,
    val audio: AudioCommentUIModel?,
    val type: String?,
    val isLiked: Boolean?,
    val likesCount: Int?,
    val ownerId: Int,
    val ownerType: String?,
    val innerComments: List<CommentUIModel>,
    val listensCount: Int?,
    val podcastId: Int,
    val links: LinkUIModel?,
    val commentCount: Int?,
) : Parcelable {

    companion object {
        const val OWNER_TYPE_COMMENT = "Comment"
        const val OWNER_TYPE_PODCAST = "Podcast"
    }
}

fun GetCommentsByPodcastsQuery.GetCommentsByPodcast.mapToUIModel() =
    CommentUIModel(
        id = id!!,
        user = user?.mapToUIModel(),
        content = content,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        mentions = mentions?.mapToUIModel(),
        tags = tags?.caption?.mapNotNull { it?.mapToUIModel() },
        isActive = active,
        audio = audio?.mapToUIModel(),
        type = type,
        isLiked = liked,
        likesCount = number_of_likes,
        ownerId = owner_id!!,
        ownerType = owner_type,
        innerComments = comments?.mapNotNull { it?.mapToUIModel() } ?: emptyList(),
        listensCount = number_of_listens,
        podcastId = podcast_id!!,
        links = links?.mapToUIModel(),
        commentCount = comment_count
    )

fun GetCommentsByPodcastsQuery.Comment.mapToUIModel() =
    CommentUIModel(
        id = id!!,
        user = user?.mapToUIModel(),
        content = content,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        mentions = mentions?.mapToUIModel(),
        tags = tags?.caption?.mapNotNull { it?.mapToUIModel() },
        isActive = active,
        audio = audio?.mapToUIModel(),
        type = type,
        isLiked = liked,
        likesCount = number_of_likes,
        ownerId = owner_id!!,
        ownerType = owner_type,
        innerComments = emptyList(), // No inner comments for a already inner comment
        listensCount = number_of_listens,
        podcastId = podcast_id!!,
        links = links?.mapToUIModel(),
        commentCount = comment_count
    )