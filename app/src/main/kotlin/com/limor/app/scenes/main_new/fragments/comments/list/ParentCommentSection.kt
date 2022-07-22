package com.limor.app.scenes.main_new.fragments.comments.list

import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentChildItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.CommentParentItem
import com.limor.app.scenes.main_new.fragments.comments.list.item.ViewMoreCommentsItem
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.Section

/**
 * Structure:
 *
 * ------------------------
 * |  CommentParentItem   |
 * | -------------------- |
 * |   CommentChildItem   | <--- Not always present
 * | -------------------- |
 * | ViewMoreCommentsItem | <--- Not always present
 * ------------------------
 */
class ParentCommentSection(
    val castOwnerId: Int,
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel, replyToComment: CommentUIModel?) -> Unit,
    val onViewMoreCommentsClick: (CommentUIModel) -> Unit,
    val onThreeDotsClick: (CommentUIModel,item: CommentParentItem, section: ParentCommentSection) -> Unit,
    val onChildThreeDotsClick: (CommentUIModel,item: CommentChildItem, section: ParentCommentSection) -> Unit,
    val onLikeClick: (comment: CommentUIModel, liked: Boolean) -> Unit,
    val onUserMentionClick: (username: String, userId: Int) -> Unit,
    val onCommentListen: (commentId: Int) -> Unit,
    val highlight: Boolean
) : Section() {

    init {
        add(
            CommentParentItem(
                castOwnerId,
                comment,
                onReplyClick = {
                    // In this case user tries to reply to the parent comment
                    onReplyClick(it, it)
                },
                onLikeClick = onLikeClick,
                onUserMentionClick = onUserMentionClick,
                onThreeDotsClick = {
                    c,i ->
                    // This is when user taps on three dots on a comment
                    onThreeDotsClick(c,i,this)
                },
                onCommentListen = onCommentListen,
                highlight = highlight
            )
        )
        comment.innerComments.firstOrNull()?.let { childComment ->
            add(
                CommentChildItem(
                    castOwnerId,
                    comment,
                    childComment,
                    isSimplified = true,
                    onReplyClick = onReplyClick,
                    onLikeClick = onLikeClick,
                    onThreeDotsClick = {
                        // This is when user taps on three dots on a comment
                        c,i,p ->
                        onChildThreeDotsClick(c,i,this)
                    },
                    onUserMentionClick = onUserMentionClick,
                    onCommentListen = onCommentListen,
                    false
                )
            )
            if (comment.innerComments.size > 1) {
                add(ViewMoreCommentsItem(comment, onViewMoreCommentsClick))
            }
        }
    }
}