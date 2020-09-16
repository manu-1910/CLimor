package entities.response


data class CreateReportResponseEntity(
    val code : Int,
    val message: String,
    val data: ReportedEntity?
)

data class ReportedEntity (
    val reported: Boolean
)