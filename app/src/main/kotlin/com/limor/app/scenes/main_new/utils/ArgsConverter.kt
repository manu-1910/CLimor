package com.limor.app.scenes.main_new.utils

import android.os.Bundle
import com.limor.app.FeedItemsQuery
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ArgsConverter {

    companion object {
        const val LABEL_DIALOG_REPORT_PODCAST = "dialog_report_podcast"

        fun encodeFeedItemAsReportDialogArgs(item: FeedItemsQuery.FeedItem): String {
            val args = ReportDialogArgs(item.podcast?.id ?: 0, item.podcast?.owner?.id ?: 0)
            return encodeReportDialogArgs(args)
        }

        fun encodeReportDialogArgs(args: ReportDialogArgs) :String{
            return Json.encodeToString(args)
        }
        fun decodeFeedItemBundleAsReportDialogArgs(bundle: Bundle): ReportDialogArgs {
            val encoded = bundle.getString(LABEL_DIALOG_REPORT_PODCAST, "{}")
            return Json.decodeFromString(encoded)
        }
        fun decodeFeedItemAsReportDialogArgs(encoded: String): ReportDialogArgs {
            return Json.decodeFromString(encoded)
        }
    }
}

@Serializable
data class ReportDialogArgs(val podcastId: Int, val ownerId: Int)
