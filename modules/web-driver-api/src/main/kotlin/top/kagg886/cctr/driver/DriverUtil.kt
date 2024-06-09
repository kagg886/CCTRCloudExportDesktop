package top.kagg886.cctr.driver

import kotlinx.coroutines.future.asDeferred
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
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

        }
        ImageIO.read(ByteArrayInputStream(rectangle!!.getScreenshotAs(OutputType.BYTES)))
    }.asDeferred().await()
}