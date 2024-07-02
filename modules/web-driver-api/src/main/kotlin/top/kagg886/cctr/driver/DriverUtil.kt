package top.kagg886.cctr.driver

import kotlinx.coroutines.future.asDeferred
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebElement
import org.openqa.selenium.edge.EdgeDriver
import top.kagg886.cctr.util.useTempFile
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.SequenceInputStream
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

fun exec(vararg cmd: String): String {
    val p = Runtime.getRuntime().exec(cmd)
    return SequenceInputStream(p.inputStream, p.errorStream).readAllBytes().decodeToString()
}

fun execWithArgs(cmd: Array<String>): String {
    val p = Runtime.getRuntime().exec(cmd)
    return SequenceInputStream(p.inputStream, p.errorStream).readAllBytes().decodeToString()
}

suspend fun EdgeDriver.captchaImage(html: String): BufferedImage {
    return CompletableFuture.supplyAsync {
        var rectangle: WebElement? = null
        useTempFile(".html") { tmp ->
            val doc: Document = Jsoup.parse("<div>$html</div>")
            doc.body().child(0).attr("id", "captcha")
            tmp.appendText(doc.html())
            //加载html
            get("file://" + tmp.absolutePath)
            rectangle = findElement(By.ById("captcha"))

            val viewPortHeight = this.executeScript("return window.innerHeight") as Long //视口大小
            val outerHeight = this.executeScript("return window.outerHeight") as Long //浏览器窗口高度
            val domHeight = this.executeScript("return document.getElementById('captcha').scrollHeight") as Long //元素实际高度

            if (domHeight > viewPortHeight) {
                with(this@captchaImage.manage().window()) {
                    size = Dimension(size.width, (domHeight + (outerHeight - viewPortHeight)).toInt())
                }
            }
        }
        ImageIO.read(ByteArrayInputStream(rectangle!!.getScreenshotAs(OutputType.BYTES)))
    }.asDeferred().await()
}