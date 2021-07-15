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
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel, replyToComment: CommentUIModel?) -> Unit,
    val onViewMoreCommentsClick: (CommentUIModel) -> Unit,
    val onLikeClick: (comment: CommentUIModel, liked: Boolean) -> Unit,
) : Section() {

    init {
        add(
            CommentParentItem(
                comment,
                onReplyClick = {
                    // In this case user tries to reply to the parent comment
                    onReplyClick(it, it)
                },
                onLikeClick = onLikeClick
            )
        )
        comment.innerComments.firstOrNull()?.let { childComment ->
            add(
                CommentChildItem(
                    comment,
                    childComment,
                    isSimplified = true,
                    onReplyClick = onReplyClick,
                    onLikeClick = onLikeClick
                )
            )
            add(ViewMoreCommentsItem(comment, onViewMoreCommentsClick))
        }
    }
}