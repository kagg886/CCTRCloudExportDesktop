import kotlinx.coroutines.runBlocking
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.getQuestionType
import top.kagg886.cctr.api.modules.getUserPracticeList
import top.kagg886.cctr.api.modules.queryQuestionList
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserTest {
    private lateinit var cctrUser: CCTRUser

    @Test
    fun testQuestionList(): Unit = runBlocking {
        println(cctrUser.getUserPracticeList())
    }

    @Test
    fun testQuestionType(): Unit = runBlocking {
        val pr = cctrUser.getUserPracticeList()[0]
        println(pr)
        println(cctrUser.getQuestionType(pr))
    }

    @Test
    fun testQuestionAll(): Unit = runBlocking {
        val pr = cctrUser.getUserPracticeList().first { it.practiceName.contains("客观题") }
        println(pr)
        val (_, qType) = cctrUser.getQuestionType(pr)

        val result = cctrUser.queryQuestionList(pr) {
            question = listOf(
                qType.first { it.name == "单选题" }
            )
        }

        for (i in result) {
            println(i.subjectHtml)
            println("----------------")
            println(i.answer)
            println("----------------")
            println(i.options.forEach {
                println(it.html)
            })
            println("----------------")
            println("----------------")
        }
    }


    @BeforeTest
    fun setup(): Unit = runBlocking {
        cctrUser = CCTRUser.newCCTRUser {
            schoolId = "U101441"
            username = "2203050528"
            password = File("password.txt").readText()
        }
    }
}