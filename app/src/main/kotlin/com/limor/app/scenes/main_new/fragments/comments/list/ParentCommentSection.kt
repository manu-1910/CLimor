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
 * | ViewMoreCommentsItem |
 * ------------------------
 */
class ParentCommentSection(
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel, childComment: CommentUIModel?) -> Unit,
    val onViewMoreCommentsClick: (CommentUIModel) -> Unit
) : Section() {

    init {
        add(
            CommentParentItem(
                comment,
                onReplyClick = {
                    onReplyClick(it, null)
                }
            )
        )
        comment.innerComments.firstOrNull()?.let { childComment ->
            add(
                CommentChildItem(
                    comment,
                    childComment,
                    isSimplified = true,
                    onReplyClick = onReplyClick
                )
            )
            add(ViewMoreCommentsItem(comment, onViewMoreCommentsClick))
        }
    }
}