package top.kagg886.cctr.api.modules

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.util.BaseResponse
import top.kagg886.cctr.api.util.CCTRException
import top.kagg886.cctr.api.util.ErrorMessage
import top.kagg886.cctr.api.util.buildURLParams

@Serializable
data class Practice(
    internal val id: Int,
    @SerialName("courseid")
    internal val courseid: String,
    @SerialName("practiseid")
    internal val practiseId: String,
    @SerialName("teacherid")
    internal val teacherId: String,


    @SerialName("lessonname")
    val lessonName: String,
    @SerialName("practicename")
    val practiceName: String,
    @SerialName("teachernumber")
    val teacherName: String,
    @SerialName("finishtime")
    val finishTime: String,
)


suspend fun CCTRUser.getUserPracticeList(): List<Practice> {
    val s = run {
        val resp =
            net.post("https://api.cctrcloud.net/mobile/index.php?act=studentpracticeapi&op=getPractiseCourseList") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    buildURLParams(
                        "key" to key
                    )
                )
            }.body<BaseResponse>()
        if (resp.code != 200) {
            throw CCTRException(resp.data<ErrorMessage>()!!.error)
        }
        resp.data<List<Practice>>()!!
    }
    return s
}