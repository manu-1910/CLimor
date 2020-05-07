package entities.response

data class DraftsResponseEntity(
    val data: ArrayList<DraftEntity>,
    val status: String
)

data class DraftEntity(
    val id: Long?,
    val title: String?,
    val caption: String?,
    val filePath: String?,
    val editedFilePath: String?,
    val tempPhotoPath: String?,
    val length: Long?,
    val time: Long?,
    val audioDuration: Int?,
    val audioStart: Int?,
    val audioEnd: Int?
)
