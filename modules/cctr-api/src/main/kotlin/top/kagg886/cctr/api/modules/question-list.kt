package top.kagg886.cctr.api.modules

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.util.*


//
class QueryModel {

    enum class PracticeType(val code: Int) {
        NO_PRACTICED(0), PRACTICED(1)
    }

    var pageCount = 15
    var practiceType: PracticeType = PracticeType.NO_PRACTICED
    var chapter: List<ChapterType>? = null
    var question: List<QuestionType>? = null
}

//{
//    "questionid": "42649539",
//    "courseid": "50794",
//    "questiontypeid": "194466",
//    "questiontypename": "计算",
//    "questiontypenumber": "5",
//    "questiontypedescription": null,
//    "chapterid": "400592",
//    "score": "8.0",
//    "subjecthtml_svg": "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",
//    "answerhtml_svg": "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",
//    "answer": "解:设随机变量Xi表示第i个加数的取整误差,则有Xi,i=1,2,8943,300,独立同分布,Xi在区间87220.5,0.5上服从均匀分布,并且有E(Xi)=0,D(Xi)=112,i=1,2,8943,300.(2分)于是P8721i=1300Xi6010=P8721i=0300Xi300/126010300/128776934(2)8722934(87222)=0.9544所以300个数相加时误差总和的绝对值小于10的概率约为0.9544。(6分)",
//    "analysishtml_svg": "",
//    "scoredimensions": "",
//    "scorestandardhtml": "",
//    "options": "",
//    "subquestions": [],
//    "id": "642337372",
//    "state": "0",
//    "iswrong": "2",
//    "iscollect": "0",
//    "ismark": "0",
//    "studentanswer": "",
//    "studentanswerhtml": ""
//}
@Serializable
data class Question(
    @SerialName("questionid")
    val id: String,

    @SerialName("questiontypename")
    val questionType: String,

    internal val subjecthtml_svg: String,
    internal val answerhtml_svg: String,
    @SerialName("answer")
    internal val answer_origin: String,

    @SerialName("options")
    internal val options_origin: JsonElement,
) {
    val subjectHtml by lazy {
        subjecthtml_svg.toByteArray().decompress().decodeToString()
    }

    val hasOptions by lazy {
        //有选项：options_origin as? JsonPrimitive 为null
        //无选项则非null
        options_origin as? JsonPrimitive == null
    }

    val options by lazy {
        json.decodeFromJsonElement<List<Option>>(options_origin)
    }

    val answer by lazy {
        if (answerhtml_svg.isEmpty()) answer_origin else answerhtml_svg.toByteArray().decompress().decodeToString()
    }
}

@Serializable
data class Option(
    internal val istrue: Int,
    internal val questionoptionhtml_svg: String
) {
    val isTrue: Boolean by lazy {
        istrue == 0
    }

    //    fun getOptionsDecode(): List<String> {
//        val s: MutableList<String> = ArrayList()
//        for (option in options.stream().map { b -> (b as JSONObject) }.collect(Collectors.toList())) {
//            var h: String = decode(option.getString("questionoptionhtml_svg"))
//            if (option.getString("istrue").equals("1")) {
//                h = "<div style=\"background: red\">$h</div>"
//            }
//            s.add(h)
//        }
//        return s
//    }
    val html by lazy {
        var ans = questionoptionhtml_svg.toByteArray().decompress().decodeToString()
        if (isTrue) {
            ans = """
                <div style="background: red;">
                    $ans
                </div>
            """.trimIndent()
        }
        ans
    }
}

suspend fun CCTRUser.queryQuestionList(practice: Practice, queryModel: QueryModel.() -> Unit = {}): List<Question> {
    @Serializable
    data class QuestionAPIReturn(
        @SerialName("questionlist")
        val questionList: List<Question>,
        val split: List<String>? = listOf(),
        val questioncount: Int,
    )

    val query = QueryModel().apply(queryModel)
    val list = mutableListOf<Question>()

    val resp =
        net.post("https://api.cctrcloud.net/mobile/index.php?act=studentpracticeapi&op=getStudentPractiseQuestionList") {
            contentType(ContentType.Application.FormUrlEncoded)

            var a1 = buildURLParams(
                "key" to key,
                "courseid" to practice.courseid,
                "practiseid" to practice.practiseId,
                "studentpractiseid" to practice.id,
                "teacherid" to practice.teacherId,

                "pagecount" to query.pageCount,
                "pageindex" to 1,

                "practisetype" to query.practiceType.code,
                "statenum" to 0,
                "id" to "",
                "list" to "true",
                "studentpractisequestioncount" to 53,
            )
            query.chapter?.apply {
                a1 += "&${
                    buildURLParams(
                        "chapterid" to query.chapter!!.map { it.id }.joinToString(",")
                    )
                }"
            }
            query.question?.apply {
                a1 += "&${
                    buildURLParams(
                        "questiontypeid" to query.question!!.map { it.id }.joinToString(",")
                    )
                }"
            }
            setBody(a1)
        }.body<BaseResponse>()

    if (resp.code != 200) {
        throw CCTRException(resp.data<ErrorMessage>()!!.error)
    }

    with(resp.data<QuestionAPIReturn>()!!) {
        list.addAll(this.questionList)
        if (questionList.isEmpty()) {
            return emptyList()
        }
        if (split!!.size > 1) {
            var page = 2
            split.subList(1, split.size).forEach { id ->
                val resp =
                    net.post("https://api.cctrcloud.net/mobile/index.php?act=studentpracticeapi&op=getStudentPractiseQuestionList") {
                        contentType(ContentType.Application.FormUrlEncoded)

                        var a1 = buildURLParams(
                            "key" to key,
                            "courseid" to practice.courseid,
                            "practiseid" to practice.practiseId,
                            "studentpractiseid" to practice.id,
                            "teacherid" to practice.teacherId,

                            "pagecount" to query.pageCount,
                            "pageindex" to page++,

                            "practisetype" to query.practiceType.code,
                            "statenum" to 0,
                            "id" to id,
                            "list" to "true",
                            "studentpractisequestioncount" to 53,
                        )
                        query.chapter?.apply {
                            a1 += "&${
                                buildURLParams(
                                    "chapterid" to query.chapter!!.map { it.id }.joinToString(",")
                                )
                            }"
                        }
                        query.question?.apply {
                            a1 += "&${
                                buildURLParams(
                                    "questiontypeid" to query.question!!.map { it.id }.joinToString(",")
                                )
                            }"
                        }
                        setBody(a1)
                    }.body<BaseResponse>()

                if (resp.code != 200) {
                    throw CCTRException(resp.data<ErrorMessage>()!!.error)
                }
                list.addAll(resp.data<QuestionAPIReturn>()!!.questionList)
            }
        }
    }

    return list
}