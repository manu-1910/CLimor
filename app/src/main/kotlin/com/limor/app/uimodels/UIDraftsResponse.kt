package com.limor.app.uimodels


import java.io.Serializable


data class UIDraftsResponse(
    var data: ArrayList<UIDraft>,
    var status: String
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
    var isEditMode: Boolean?,
    var timeStamps: ArrayList<UITimeStamp>?,
    var date: String?,
    var categoryId: Int?,
    var languageCode: String?,
    var language: String?,
    var category: String?,
    var location: UILocations?,
    var draftParent : UIDraft?,
    var isNewRecording : Boolean,
    var categories: List<UISimpleCategory>?
): Serializable {
    constructor() : this( 0,"", "", "", "", "", 0, 0, false, ArrayList(), "", 0, "","","", UILocations(), null, false, null)
}


data class UITimeStamp(
    var duration: Int?,
    var startSample: Int?,
    var endSample: Int?
): Serializable {
    constructor() : this( 0,0, 0)
}


data class UISimpleCategory(
    val name: String,
    val categoryId: Int
)
