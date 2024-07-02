import kotlinx.coroutines.runBlocking
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.QueryModel
import top.kagg886.cctr.api.modules.getQuestionType
import top.kagg886.cctr.api.modules.getUserPracticeList
import top.kagg886.cctr.api.modules.queryQuestionList
import top.kagg886.cctr.desktop.util.edge_config
import top.kagg886.cctr.desktop.util.getEdgeDriverFile
import top.kagg886.cctr.desktop.util.msedgedriverName
import top.kagg886.cctr.driver.WebDriverDispatcher
import top.kagg886.cctr.driver.captchaImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class PDFTest {
    @Test
    fun testLongImageCaptcha():Unit = runBlocking {
        WebDriverDispatcher.init {
            driverFile = getEdgeDriverFile(edge_config.get { this.version }).resolve(msedgedriverName)
            driverPoolSize = 1
            executableFile = File(edge_config.get { binary })
            headless = false
        }

        WebDriverDispatcher.useDriver { driver->
            val user = CCTRUser.newCCTRUser {
                schoolId = "schoolId"
                username = "stuCode"
                password = "123456"
            }
            val practice = user.getUserPracticeList().first { it.practiceName.contains("数据库") }
            println(practice)
            val cType = user.getQuestionType(practice).first.first { it.name.contains("SQL") }
            println(cType)
            val q = user.queryQuestionList(practice) {
                chapter = listOf(cType)
                practiceType = QueryModel.PracticeType.PRACTICED
            }
            println(q.size)
            q.map {
                """
                    <div style="padding: 30px">
                    <span>答：</span>
                        ${it.answer}
                    </div>
                """.trimIndent()
            }.withIndex().forEach { (key,value)->
                val buf = driver.captchaImage(value)
                ImageIO.write(buf, "png", File("out").resolve("${key}.png").apply {
                    if (exists()) {
                        delete()
                    }
                    parentFile.mkdirs()
                    createNewFile()
                })
            }
        }
    }
}