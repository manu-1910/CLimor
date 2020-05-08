package io.square1.limor.uimodels


import java.io.Serializable
import kotlin.collections.ArrayList

data class UIDraftsResponse(
    val data: ArrayList<UIDraft>,
    val status: String
)


data class UIDraft(
    var id: Long?,
    var title: String?,
    var caption: String?,
    var filePath: String?,
    var editedFilePath: String?,
    var tempPhotoPath: String?,
    var length: Long?,
    var time: Long?,
    var audioDuration: Int?,
    var audioStart: Int?,
    var audioEnd: Int?,
    var isEditMode: Boolean?

): Serializable {
    constructor() : this( 0,"", "", "", "", "", 0, 0, 0, 0, 0, false)
}