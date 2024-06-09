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

//{
//    "code": 200,
//    "datas": {
//        "chapterList": [
//            {
//                "id": "412466",
//                "courseid": "60152",
//                "name": "第1章 单向静拉伸力学性能"
//            }
//        ],
//        "questionTypeList": [
//            {
//                "questiontypeid": "174499",
//                "id": "174499",
//                "number": "4",
//                "cname": "填空题",
//                "issuper": "1"
//            },
//        ]
//    }
//}

@Serializable
data class ChapterType(
    val id: String,
    @SerialName("courseid")
    val courseId:String,
    val name: String,


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChapterType

        if (id != other.id) return false
        if (courseId != other.courseId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + courseId.hashCode()
        return result
    }
}

@Serializable
data class QuestionType(
    @SerialName("questiontypeid")
    val questionTypeId:String,
    val id:String,
    @SerialName("cname")
    val name: String,


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestionType

        if (questionTypeId != other.questionTypeId) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = questionTypeId.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}

suspend fun CCTRUser.getQuestionType(practice: Practice): Pair<List<ChapterType>, List<QuestionType>> {

    @Serializable
    data class QTResponse(
        val chapterList: List<ChapterType>,
        val questionTypeList: List<QuestionType>,
    )

    val resp =
        net.post("https://api.cctrcloud.net/mobile/index.php?act=studentpracticeapi&op=getChapterListAndQuestionTypeList") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                buildURLParams(
                    "key" to key,
                    "courseid" to practice.courseid,
                    "practiseid" to practice.practiseId
                )
            )
        }.body<BaseResponse>()
    if (resp.code != 200) {
        throw CCTRException(resp.data<ErrorMessage>()!!.error)
    }
    return resp.data<QTResponse>()!!.let {
        it.chapterList to it.questionTypeList
    }
}
