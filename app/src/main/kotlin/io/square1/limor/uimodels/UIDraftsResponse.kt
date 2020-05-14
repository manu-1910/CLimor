package io.square1.limor.uimodels


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
    var timeStamps: ArrayList<UITimeStamp>?
): Serializable {
    constructor() : this( 0,"", "", "", "", "", 0, 0, false, ArrayList())
}


data class UITimeStamp(
    var duration: Int?,
    var startSample: Int?,
    var endSample: Int?
): Serializable {
    constructor() : this( 0,0, 0)
}



