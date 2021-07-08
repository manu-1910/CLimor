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
    val isEditMode: Boolean?,
    val timeStamps: ArrayList<TimeStampEntity>,
    val date: String?,
    val categoryId: Int?,
    val languageCode: String?,
    val language: String?,
    val category: String?,
    val location: LocationsEntity?,
    val parentDraft : DraftEntity?,
    val isNewRecording : Boolean
)


data class TimeStampEntity(
    val duration: Int?,
    val startSample: Int?,
    val endSample: Int?
)